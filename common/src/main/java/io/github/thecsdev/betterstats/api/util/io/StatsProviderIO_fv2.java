package io.github.thecsdev.betterstats.api.util.io;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.betterstats.api.util.stats.SUPlayerBadgeStat;
import io.github.thecsdev.tcdcommons.api.util.exceptions.UnsupportedFileVersionException;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * A class containing {@link StatsProviderIO} logic for file version 2.
 * @apiNote Used for backwards-compatibility. Internal use only!
 */
@Internal
public final class StatsProviderIO_fv2
{
	// ==================================================
	static final void write_fileChunks(FriendlyByteBuf buffer_file, IStatsProvider statsProvider)
	{
		//write chunks
		write_fileChunk("metadata", buffer_file, statsProvider);
		write_fileChunk("general", buffer_file, statsProvider);
		write_fileChunk("item", buffer_file, statsProvider);
		write_fileChunk("mob", buffer_file, statsProvider);
		write_fileChunk("player_badge", buffer_file, statsProvider);
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
			case "metadata":     write_fileChunk_meta(buffer_chunk, statsProvider); break;
			case "general":      write_fileChunk_general(buffer_chunk, statsProvider); break;
			case "item":         write_fileChunk_item(buffer_chunk, statsProvider); break;
			case "mob":          write_fileChunk_mob(buffer_chunk, statsProvider); break;
			case "player_badge": write_fileChunk_playerBadge(buffer_chunk, statsProvider); break;
			default: break;
		}
		
		//write the chunk data buffer to the file buffer
		buffer_file.writeIntLE(buffer_chunk.readableBytes());
		buffer_file.writeBytes(buffer_chunk);
		buffer_chunk.release();
	}
	
	@SuppressWarnings("deprecation")
	private static final void write_fileChunk_meta(FriendlyByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//write display name
		@Nullable Component displayName = statsProvider.getDisplayName();
		if(displayName == null) displayName = literal("-");
		buffer_chunk.writeWithCodec(NbtOps.INSTANCE, ComponentSerialization.CODEC, displayName);
		
		//write game profile
		writeGameProfile(buffer_chunk, statsProvider.getGameProfile());
	}
	
	private static final void write_fileChunk_general(FriendlyByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//NOTE: Generally, this part is highly confusing, not only because "general stats" are referred to as
		//      "custom stats" internally, but also because here the stats are being stored in accordance
		//      to their REGISTRY Identifier-s, and NOT the Stat<Identifier> Identifier-s.
		//
		//      Aka, the Registry holding custom stats is a Map of Identifier-s, whose keys are also Identifier-s.
		//      Here, the stats are stored in accordance to the Registry aka Map KEYS, NOT VALUES!
		
		//obtain a map of the general stats, that maps the stats based on their identifier's namespaces,
		//aka group based on the "mod id" of the mod they belong to
		final var customStats = Lists.newArrayList(BuiltInRegistries.CUSTOM_STAT.iterator());
		final var customStatsMap = customStats.stream()
				.filter(stat -> BuiltInRegistries.CUSTOM_STAT.getKey(stat) != null) //ignore incompatibilities and unregistered stats
				.filter(stat -> statsProvider.getStatValue(Stats.CUSTOM.get(stat)) != 0)
				.collect(Collectors.groupingBy(stat -> BuiltInRegistries.CUSTOM_STAT.getKey(stat).getNamespace()));
		
		//iterate groups, obtain and write their data
		for(final var entry : customStatsMap.entrySet())
		{
			//obtain the group id and its stats
			final var groupModId = entry.getKey();
			final var groupStats = entry.getValue();
			
			//write the group id and its length
			buffer_chunk.writeUtf(groupModId); //write group id
			buffer_chunk.writeVarInt(groupStats.size()); //write group length
			
			//write group entries [Identifier-path, VarInt-value]
			for(final ResourceLocation customStatAsIdentifier : groupStats)
			{
				//obtain the custom stat and its value
				final Stat<ResourceLocation> customStat = Stats.CUSTOM.get(customStatAsIdentifier);
				final int customStatValue = statsProvider.getStatValue(customStat);
				
				//write the custom stat registry id path and its value
				buffer_chunk.writeUtf(customStatAsIdentifier.getPath());
				buffer_chunk.writeVarInt(customStatValue);
			}
		}
	}
	
	private static final void write_fileChunk_item(FriendlyByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//obtain a map of item stats
		final var stats = SUItemStat.getItemStatsByModGroups(statsProvider, stat -> !stat.isEmpty());
		
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
				buffer_chunk.writeVarInt(stat.mined);
				buffer_chunk.writeVarInt(stat.crafted);
				buffer_chunk.writeVarInt(stat.used);
				buffer_chunk.writeVarInt(stat.broken);
				buffer_chunk.writeVarInt(stat.pickedUp);
				buffer_chunk.writeVarInt(stat.dropped);
			}
		}
	}
	
	private static final void write_fileChunk_mob(FriendlyByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//obtain a map of mod stats
		final var stats = SUMobStat.getMobStatsByModGroups(statsProvider, stat -> !stat.isEmpty());
		
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
				buffer_chunk.writeVarInt(stat.kills);
				buffer_chunk.writeVarInt(stat.deaths);
			}
		}
	}
	
	private static final void write_fileChunk_playerBadge(FriendlyByteBuf buffer_chunk, IStatsProvider statsProvider)
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
				case "metadata":     read_fileChunk_meta(buffer_chunk, statsProvider); break;
				case "general":      read_fileChunk_general(buffer_chunk, statsProvider); break;
				case "item":         read_fileChunk_item(buffer_chunk, statsProvider); break;
				case "mob":          read_fileChunk_mob(buffer_chunk, statsProvider); break;
				case "player_badge": read_fileChunk_playerBadge(buffer_chunk, statsProvider); break;
				default: break;
			}
		}
	}
	// --------------------------------------------------
	@SuppressWarnings("deprecation")
	private static final void read_fileChunk_meta(FriendlyByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		//read display name
		final Component displayName = buffer_chunk.readWithCodec(NbtOps.INSTANCE, ComponentSerialization.CODEC, NbtAccounter.create(2097152L));
		statsProvider.setDisplayName(displayName);
		
		//read game profile
		if(buffer_chunk.readableBytes() < 2) return; //compatibility with alpha files
		final @Nullable GameProfile gameProfile = readGameProfile(buffer_chunk);
		statsProvider.setGameProfile(gameProfile);
	}
	
	private static final void read_fileChunk_general(FriendlyByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		while(buffer_chunk.readableBytes() > 0)
		{
			//read the next mod id and how many entries it has
			final String modId = buffer_chunk.readUtf();
			final int entryCount = buffer_chunk.readVarInt();
			
			//read all entries for the corresponding mod id
			for(int i = 0; i < entryCount; i++)
			{
				//read custom stat data
				final String customStatIdPath = buffer_chunk.readUtf();
				final int customStatValue = buffer_chunk.readVarInt();
				
				//obtain custom stat, and sore its value
				//comment: the fact that the stat itself and its key are both Identifier-s always confuses me
				final ResourceLocation customStatId = ResourceLocation.fromNamespaceAndPath(modId, customStatIdPath);
				final ResourceLocation customStat = BuiltInRegistries.CUSTOM_STAT.getValue(customStatId);
				if(customStat == null) continue; //for now, unknown modded stats are ignored
				
				//set stat value
				statsProvider.setStatValue(Stats.CUSTOM.get(customStat), customStatValue);
			}
		}
	}
	
	private static final void read_fileChunk_item(FriendlyByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		while(buffer_chunk.readableBytes() > 0)
		{
			//read the next mod id and how many entries it has
			final String modId = buffer_chunk.readUtf();
			final int entryCount = buffer_chunk.readVarInt();
			
			//read all entries for the corresponding mod id
			for(int i = 0; i < entryCount; i++)
			{
				//read item stat data
				final String itemIdPath = buffer_chunk.readUtf();
				final int mined  = buffer_chunk.readVarInt(),
						crafted  = buffer_chunk.readVarInt(),
						used     = buffer_chunk.readVarInt(),
						broken   = buffer_chunk.readVarInt(),
						pickedUp = buffer_chunk.readVarInt(),
						dropped  = buffer_chunk.readVarInt();
				
				//obtain item, and store its stats
				final ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(modId, itemIdPath);
				final @Nullable Item item = BuiltInRegistries.ITEM.getValue(itemId);
				final @Nullable Block block = (item != null) ? Block.byItem(item) : null;
				
				if(item == null) continue; //for now, unknown modded stats are ignored
				else
				{
					if(block != null) statsProvider.setStatValue(Stats.BLOCK_MINED, block, mined);
					statsProvider.setStatValue(Stats.ITEM_CRAFTED,   item, crafted);
					statsProvider.setStatValue(Stats.ITEM_USED,      item, used);
					statsProvider.setStatValue(Stats.ITEM_BROKEN,    item, broken);
					statsProvider.setStatValue(Stats.ITEM_PICKED_UP, item, pickedUp);
					statsProvider.setStatValue(Stats.ITEM_DROPPED,   item, dropped);
				}
			}
		}
	}
	
	private static final void read_fileChunk_mob(FriendlyByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		while(buffer_chunk.readableBytes() > 0)
		{
			//read the next mod id and how many entries it has
			final String modId = buffer_chunk.readUtf();
			final int entryCount = buffer_chunk.readVarInt();
			
			//read all entries for the corresponding mod id
			for(int i = 0; i < entryCount; i++)
			{
				//read mob stat data
				final String mobIdPath = buffer_chunk.readUtf();
				final int kills = buffer_chunk.readVarInt();
				final int deaths = buffer_chunk.readVarInt();
				
				//obtain mob, and store its stats
				final ResourceLocation mobId = ResourceLocation.fromNamespaceAndPath(modId, mobIdPath);
				final @Nullable EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(mobId);
				
				if(entityType == null) continue; //for now, unknown modded stats are ignored
				else
				{
					statsProvider.setStatValue(Stats.ENTITY_KILLED, entityType, kills);
					statsProvider.setStatValue(Stats.ENTITY_KILLED_BY, entityType, deaths);
				}
			}
		}
	}
	
	private static final void read_fileChunk_playerBadge(FriendlyByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
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
	private static final void writeGameProfile(FriendlyByteBuf buffer, @Nullable GameProfile gameProfile)
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
	
	private static final @Nullable GameProfile readGameProfile(FriendlyByteBuf buffer)
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
}