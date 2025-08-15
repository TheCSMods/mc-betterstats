package io.github.thecsdev.betterstats.api.util.io;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.util.stats.SUPlayerBadgeStat;
import io.github.thecsdev.tcdcommons.api.util.exceptions.UnsupportedFileVersionException;
import io.netty.buffer.Unpooled;

public class StatsProviderIO_fv4
{
	// ==================================================
	static final void write_fileChunks(FriendlyByteBuf buffer_file, IStatsProvider statsProvider)
	{
		write_fileChunk("metadata", buffer_file, statsProvider);
		write_fileChunk("stats", buffer_file, statsProvider);
		write_fileChunk("tcdcommons:player_badges", buffer_file, statsProvider);
	}
	// --------------------------------------------------
	private static final void write_fileChunk(String chunkId, FriendlyByteBuf buffer_file, IStatsProvider statsProvider)
	{
		//create a buffer for the chunk, and write the chunk ID to it
		FriendlyByteBuf buffer_chunk  = new FriendlyByteBuf(Unpooled.buffer());
		buffer_chunk.writeUtf(chunkId);
		
		//obtain and write chunk data to the chunk buffer
		switch(chunkId)
		{
			case "metadata": write_fileChunk_metadata(buffer_chunk, statsProvider); break;
			case "stats": write_fileChunk_stats(buffer_chunk, statsProvider); break;
			case "tcdcommons:player_badges": write_fileChunk_playerBadges(buffer_chunk, statsProvider); break;
			default: break;
		}
		
		//write the chunk data buffer to the file buffer
		buffer_file.writeIntLE(buffer_chunk.readableBytes());
		buffer_file.writeBytes(buffer_chunk);
		buffer_chunk.release();
	}
	// --------------------------------------------------
	private static final void write_fileChunk_metadata(FriendlyByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//obtain the stats display name as string
		final var statsNameText = statsProvider.getDisplayName();
		final var statsName = (statsNameText != null) ? statsNameText.getString() : "-";
		
		//write name and game profile
		buffer_chunk.writeUtf(statsName);
		StatsProviderIO.writeGameProfile(buffer_chunk, statsProvider.getGameProfile());
	}
	// --------------------------------------------------
	@SuppressWarnings("unchecked")
	private static final void write_fileChunk_stats(FriendlyByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//iterate all stat types, and write their corresponding stat data one by one
		for(final var statType : BuiltInRegistries.STAT_TYPE)
		{
			//create a buffer for the stat type chunk, and write the chunk ID to it
			FriendlyByteBuf buffer_st  = new FriendlyByteBuf(Unpooled.buffer());
			buffer_st.writeUtf(Objects.toString(BuiltInRegistries.STAT_TYPE.getKey(statType)));        //write chunk id
			
			//write the stats data for the given stats type, to the stats type buffer
			write_fileChunk_stats_statType(buffer_st, statsProvider, (StatType<Object>)statType); //write chunk data
			
			//write the stat type buffer data to the chunk buffer
			buffer_chunk.writeIntLE(buffer_st.readableBytes());
			buffer_chunk.writeBytes(buffer_st);
			buffer_st.release();
		}
	}
	
	private static final void write_fileChunk_stats_statType(
			FriendlyByteBuf buffer_st, IStatsProvider statsProvider, StatType<Object> statType)
	{
		//obtain the registry, and iterate all of its items
		final var registry = statType.getRegistry();
		for(final var registryItem : registry)
		{
			//obtain the stat value for the given registry item
			//also skip "zero" stats, as they do not have a value
			final int statValue = statsProvider.getStatValue(statType, registryItem);
			if(statValue == 0) continue;
			
			//obtain the id of the registry item
			final var registryItemId = registry.getKey(registryItem);
			
			//write stat id and value
			buffer_st.writeUtf(registryItemId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ?
					registryItemId.getPath() : Objects.toString(registryItemId)); //write stat id
			buffer_st.writeIntLE(statValue);                                      //write stat value
		}
	}
	// --------------------------------------------------
	private static final void write_fileChunk_playerBadges(FriendlyByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//obtain a map of mod stats
		final var stats = SUPlayerBadgeStat.getPlayerBadgeStatsByModGroups(statsProvider, stat -> !stat.isEmpty());
		
		//iterate groups, and write their data
		for(final var entry : stats.entrySet())
		{
			//obtain the group id and its stats
			final var groupModId = entry.getKey();
			final var groupStats = entry.getValue();
			
			//write the group id and its length
			buffer_chunk.writeUtf(groupModId);
			buffer_chunk.writeVarInt(groupStats.size());
			
			//write group entries
			for(final var stat : groupStats)
			{
				buffer_chunk.writeUtf(stat.getStatID().getPath());
				buffer_chunk.writeVarInt(stat.value);
			}
		}
	}
	// ==================================================
	static final void read_fileChunks(FriendlyByteBuf buffer_file, IEditableStatsProvider statsProvider)
			throws IllegalHeaderException, UnsupportedFileVersionException
	{
		//read chunks
		while(buffer_file.readableBytes() > 0)
		{
			//read next chunk's size, and check it
			final int chunkSize = buffer_file.readIntLE();
			if(buffer_file.readableBytes() < chunkSize)
				throw new IllegalHeaderException(
						"chunk size >= " + chunkSize,
						"chunk size == " + buffer_file.readableBytes());
			
			//read the chunk data
			//(creates a view of the original buffer, so it doesn't have to be released separately)
			final var buffer_chunk = new FriendlyByteBuf(buffer_file.readSlice(chunkSize));
			final var chunkId = buffer_chunk.readUtf();
			switch(chunkId)
			{
				case "metadata": read_fileChunk_metadata(buffer_chunk, statsProvider); break;
				case "stats": read_fileChunk_stats(buffer_chunk, statsProvider); break;
				case "tcdcommons:player_badges": read_fileChunk_playerBadges(buffer_chunk, statsProvider); break;
				default: break;
			}
		}
	}
	// --------------------------------------------------
	private static final void read_fileChunk_metadata(FriendlyByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		statsProvider.setDisplayName(literal(buffer_chunk.readUtf()));
		statsProvider.setGameProfile(StatsProviderIO.readGameProfile(buffer_chunk));
	}
	// --------------------------------------------------
	private static final void read_fileChunk_stats(FriendlyByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
			throws IllegalHeaderException
	{
		//keep reading chunks as they come in
		while(buffer_chunk.readableBytes() > 0)
		{
			//read next chunk's size, and check it
			final int chunkSize = buffer_chunk.readIntLE();                            //read stats type chunk size
			if(buffer_chunk.readableBytes() < chunkSize)
				throw new IllegalHeaderException(
						"chunk size >= " + chunkSize,
						"chunk size == " + buffer_chunk.readableBytes());
			
			//read the chunk data
			//(creates a view of the original buffer, so it doesn't have to be released separately)
			final var buffer_st = new FriendlyByteBuf(buffer_chunk.readSlice(chunkSize)); //read stats type chunk data
			read_fileChunk_stats_statType(buffer_st, statsProvider);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final void read_fileChunk_stats_statType(FriendlyByteBuf buffer_st, IEditableStatsProvider statsProvider)
	{
		//read the stat type identifier
		final var statTypeId = ResourceLocation.parse(buffer_st.readUtf());
		
		//obtain the stat type and check if it exists
		final @Nullable var statType = BuiltInRegistries.STAT_TYPE.containsKey(statTypeId) ?
				(StatType<Object>)BuiltInRegistries.STAT_TYPE.getOptional(statTypeId).get() : null;
		if(statType == null) return;
		final var statTypeRegistry = statType.getRegistry();
		
		//read stats one by one
		while(buffer_st.readableBytes() > 0)
		{
			//read stat id and stat value
			final ResourceLocation statId = ResourceLocation.parse(buffer_st.readUtf());
			final int statValue = buffer_st.readIntLE();
			
			//obtain the registry item, null check it, and store its value to the stats provider
			final @Nullable var item = statTypeRegistry.getOptional(statId).orElse(null);
			if(item == null) continue;
			statsProvider.setStatValue(statType, item, statValue);
		}
	}
	// --------------------------------------------------
	private static final void read_fileChunk_playerBadges(FriendlyByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		while(buffer_chunk.readableBytes() > 0)
		{
			//read the next mod id and how many entries it has
			final String modId = buffer_chunk.readUtf();
			final int entryCount = buffer_chunk.readVarInt();
			
			//read all entries for the corresponding mod id
			for(int i = 0; i < entryCount; i++)
			{
				//read player badge stat data
				final String playerBadgeIdPath = buffer_chunk.readUtf();
				final int value = buffer_chunk.readVarInt();
				
				//obtain mob, and store its stats
				final ResourceLocation playerBadgeId = ResourceLocation.fromNamespaceAndPath(modId, playerBadgeIdPath);
				statsProvider.setPlayerBadgeValue(playerBadgeId, value);
			}
		}
	}
	// ==================================================
}