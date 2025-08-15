package io.github.thecsdev.betterstats.api.util.io;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.util.exceptions.UnsupportedFileVersionException;
import io.netty.buffer.Unpooled;

/**
 * A utility class for reading and writing {@link IStatsProvider} data.
 */
public final class StatsProviderIO extends Object
{
	// ==================================================
	private StatsProviderIO() {}
	// --------------------------------------------------
	public static final String FILE_EXTENSION = "mcbs"; //Note: as per RIFF rules, length MUST BE 4!
	
	/**
	 * The current {@link #FILE_EXTENSION}'s "RIFF" data format version.
	 * @apiNote Changing the {@link #FILE_VERSION} is more of a "last resort"-type thing,
	 * where the overall structure of the RIFF format has to change and/or for when the game's
	 * stats system is modified by the game developers. When possible, instead of increasing the
	 * version number to change a chunk's structure, define a new chunks with its new ID, while
	 * still maintaining support for the older chunks.
	 * @apiNote TLDR; Only increase if backwards compatibility is impossible.
	 */
	public static final int FILE_VERSION = 4;
	/* # File version history:
	 * 1 - Since v3.0-alpha.1 - Initial version
	 * 2 - Since v3.0-alpha.3 - Major changes to the player badge system
	 * 3 - Since v3.9         - [Reserved for special use-case]
	 * 4 - Since v3.9         - Support for modded stat types
	 */
	// ==================================================
	/**
	 * Writes an {@link IStatsProvider}'s statistics data to a given {@link FriendlyByteBuf}.
	 * @param buffer The buffer to write the data to.
	 * @param statsProvider The data to write.
	 * @apiNote Uses the "RIFF" file format.
	 */
	public static final void write(FriendlyByteBuf buffer, IStatsProvider statsProvider)
	throws NullPointerException
	{
		//null checks
		Objects.requireNonNull(statsProvider);
		Objects.requireNonNull(buffer);
		
		//write RIFF
		buffer.writeBytes("RIFF".getBytes(US_ASCII));
		
		//write data
		write_file(buffer, statsProvider, FILE_VERSION);
	}
	
	private static final void write_file(FriendlyByteBuf buffer, IStatsProvider statsProvider, int fileVersion)
	{
		if(fileVersion < 1) throw new IllegalArgumentException("Attempting to write file version < 1.");
		
		//create the buffer
		final var buffer_file = new FriendlyByteBuf(Unpooled.buffer());
		
		//write the file extension
		if(FILE_EXTENSION.length() != 4) //RIFF specification requires length of 4
			throw new IllegalStateException("Illegal file extension length, must be 4! Current value: " + FILE_EXTENSION);
		buffer_file.writeBytes(FILE_EXTENSION.toUpperCase().getBytes(US_ASCII));

		//write the file version
		buffer_file.writeIntLE(fileVersion);
		
		//write chunks
		switch(fileVersion)
		{
			case 2: StatsProviderIO_fv2.write_fileChunks(buffer_file, statsProvider); break;
			case 4: StatsProviderIO_fv4.write_fileChunks(buffer_file, statsProvider); break;
			default: break;
		}

		//finally, write the file chunk and release the file buffer
		buffer.writeIntLE(buffer_file.readableBytes());
		buffer.writeBytes(buffer_file);
		buffer_file.release();
	}
	// ==================================================
	/**
	 * Reads an {@link IStatsProvider}'s data that has been written to a
	 * {@link FriendlyByteBuf}, and converts it to an {@link IStatsProvider}.
	 * @param buffer The buffer to read data from.
	 * @param statsProvider The {@link IEditableStatsProvider} to load the data into.
	 * @throws IllegalHeaderException If the "RIFF" header is missing, or the file extension is invalid.
	 * @throws UnsupportedFileVersionException If the file data version is not supported.
	 */
	public static final void read(FriendlyByteBuf buffer, IEditableStatsProvider statsProvider)
			throws IllegalHeaderException, UnsupportedFileVersionException
	{
		//create a new editable stats provider
		if(buffer.readableBytes() < 8)
			throw new IllegalHeaderException("chunk size >= 8", "chunk size == " + buffer.readableBytes());
		
		//first, mark the reading index, for error correction purposes
		buffer.markReaderIndex();
		
		//begin reading
		try
		{
			//read RIFF
			final String RIFF = buffer.readSlice(4).toString(US_ASCII);
			if(!"RIFF".equalsIgnoreCase(RIFF))
				throw new IllegalHeaderException("RIFF", RIFF);
			
			//read file chunk length
			final int fileLength = buffer.readIntLE();
			if(buffer.readableBytes() < fileLength)
				throw new IllegalHeaderException("chunk size >= " + fileLength, "chunk size == " + buffer.readableBytes());
			
			//begin reading file
			//(creates a view of the original buffer, so it doesn't have to be released separately)
			final var buffer_file = new FriendlyByteBuf(buffer.readSlice(fileLength));
			read_file(buffer_file, statsProvider);
		}
		catch(IllegalHeaderException | UnsupportedFileVersionException exc) { buffer.resetReaderIndex(); throw exc; }
	}
	// --------------------------------------------------
	private static final void read_file(FriendlyByteBuf buffer_file, IEditableStatsProvider statsProvider)
			throws IllegalHeaderException, UnsupportedFileVersionException
	{		
		//read file extension
		final String FEXT = buffer_file.readSlice(4).toString(US_ASCII);
		if(!FILE_EXTENSION.equalsIgnoreCase(FEXT))
			throw new IllegalHeaderException(FILE_EXTENSION, FEXT);
		
		//read file version
		final int fileVersion = buffer_file.readIntLE();
		
		//read file chunks
		switch(fileVersion)
		{
			case 2: StatsProviderIO_fv2.read_fileChunks(buffer_file, statsProvider); break;
			case 4: StatsProviderIO_fv4.read_fileChunks(buffer_file, statsProvider); break;
			default: throw new UnsupportedFileVersionException(Integer.toString(fileVersion));
		}
	}
	// ==================================================
	/**
	 * Writes a {@link GameProfile} to a given {@link FriendlyByteBuf}.
	 * @apiNote {@link GameProfile#getProperties()} are not written to the buffer.
	 */
	public static final void writeGameProfile(FriendlyByteBuf buffer, @Nullable GameProfile gameProfile)
	{
		//if game profile is null, write false for all fields
		if(gameProfile == null)
		{
			buffer.writeBoolean(false);
			buffer.writeBoolean(false);
			return;
		}
		
		//obtain game profile info
		final var uuid  = gameProfile.getId();
		final var name = gameProfile.getName();
		
		//write
		// - first UUID
		if(uuid != null) { buffer.writeBoolean(true); buffer.writeUUID(uuid); }
		else buffer.writeBoolean(false);
		// - then name
		if(name != null) { buffer.writeBoolean(true); buffer.writeUtf(name); }
		else buffer.writeBoolean(false);
	}
	
	/**
	 * Reads a {@link GameProfile} from a {@link FriendlyByteBuf}. Will return
	 * {@code null} if a "{@code null}" {@link GameProfile} was written to the buffer.
	 * @apiNote {@link GameProfile#getProperties()} are not read from the buffer.
	 */
	public static final @Nullable GameProfile readGameProfile(FriendlyByteBuf buffer)
	{
		//first UUID
		final UUID uuid = buffer.readBoolean() ? buffer.readUUID() : null;
		//then name
		final String name = buffer.readBoolean() ? buffer.readUtf() : null;
		
		//construct the game profile
		if(name == null && uuid == null) return null;
		else return new GameProfile(uuid, name);
	}
	// ==================================================
	/**
	 * Saves an {@link IStatsProvider}'s data to a {@link File}.<br/>
	 * If the {@link File} doesn't exist, it will be created; otherwise, it will be overridden.
	 * @param file The {@link File} to save the {@link IStatsProvider} to.
	 * @param statsProvider The {@link IStatsProvider} to save.
	 * @throws IOException If the {@link File} IO operations raise an {@link IOException}.
	 */
	public static void saveToFile(File file, IStatsProvider statsProvider) throws IOException
	{
		//requirements
		Objects.requireNonNull(file);
		Objects.requireNonNull(statsProvider);
		
		//create the buffer
		final var buffer = new FriendlyByteBuf(Unpooled.buffer());
		try
		{
			//create the file
			file.getParentFile().mkdirs();
			file.createNewFile();
			
			//write the data to the file
			try(final var fos = new FileOutputStream(file); final var fileChannel = fos.getChannel())
			{
				StatsProviderIO.write(buffer, statsProvider);
				while(buffer.isReadable())
					buffer.readBytes(fileChannel, buffer.readableBytes());
			}
		}
		catch(SecurityException se) { throw new IOException(se); }
		finally { buffer.release(); }
	}
	
	/**
	 * Loads an {@link IStatsProvider}'s data from a {@link File}.
	 * @param file The {@link File} to load the {@link IStatsProvider} from.
	 * @return The loaded {@link IStatsProvider}.
	 * @throws FileNotFoundException If the {@link File} does not exist.
	 * @throws IOException If {@link File} IO operations raise an {@link IOException}.
	 */
	public static IEditableStatsProvider loadFromFile(File file) throws FileNotFoundException, IOException
	{
		//check if the file exists
		if(!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());
		//load the file data
		byte[] fileData = FileUtils.readFileToByteArray(file);
		final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(fileData));
		return new RAMStatsProvider(buffer, true);
	}
	// ==================================================
}