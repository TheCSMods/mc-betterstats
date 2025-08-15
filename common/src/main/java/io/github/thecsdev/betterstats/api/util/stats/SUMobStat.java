package io.github.thecsdev.betterstats.api.util.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fTranslatable;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.tcdcommons.api.util.TUtils;

public final class SUMobStat extends SUStat<EntityType<?>>
{
	// ==================================================
	protected final EntityType<?> entityType;
	protected final boolean isEmpty; //cached value to avoid re-calculations
	// --------------------------------------------------
	public final int kills, deaths;
	// ==================================================
	public SUMobStat(IStatsProvider statsProvider, EntityType<?> entityType)
	{
		super(statsProvider, BuiltInRegistries.ENTITY_TYPE.getKey(Objects.requireNonNull(entityType)), getMobStatText(entityType));
		this.entityType = entityType;
		
		this.kills = statsProvider.getStatValue(Stats.ENTITY_KILLED, entityType);
		this.deaths = statsProvider.getStatValue(Stats.ENTITY_KILLED_BY, entityType);
		this.isEmpty = (this.kills == 0 && this.deaths == 0);
	}
	// ==================================================
	/**
	 * Returns the {@link EntityType} corresponding with this {@link SUMobStat}.
	 */
	public final EntityType<?> getEntityType() { return this.entityType; }
	// --------------------------------------------------
	public final @Override boolean isEmpty() { return this.isEmpty; }
	// ==================================================
	/**
	 * Returns the {@link Component} that should correspond to a given {@link SUMobStat}.
	 */
	public static final Component getMobStatText(EntityType<?> entityType) { return fTranslatable(entityType.getDescriptionId()); }
	// ==================================================
	/**
	 * Obtains all "mob" {@link Stat}s, in form of {@link SUMobStat}.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUMobStat}s.
	 */
	public static List<SUMobStat> getMobStats
	(IStatsProvider statsProvider, @Nullable Predicate<SUMobStat> filter)
	{
		//create the result list
		final var result = new ArrayList<SUMobStat>();
		
		//iterate all entity types
		for(final EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE)
		{
			//create the mob stat
			final var mobStat = new SUMobStat(statsProvider, entityType);
			
			//filter
			if(filter != null && !filter.test(mobStat))
				continue;
			
			//add to the list
			result.add(mobStat);
		}
		
		//return the result
		return result;
	}
	
	/**
	 * Obtains all "mob" {@link Stat}s, in form of {@link SUMobStat}, grouped
	 * into "mod groups" using a {@link Map}. The {@link Map} keys represent "mod IDs".
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUMobStat}s.
	 */
	public static Map<String, List<SUMobStat>> getMobStatsByModGroups
	(IStatsProvider statsProvider, @Nullable Predicate<SUMobStat> filter)
	{
		//create a new list
		final var result = new LinkedHashMap<String, List<SUMobStat>>();
		
		//add the 'minecraft' category first
		final String mcModId = ResourceLocation.DEFAULT_NAMESPACE;
		result.put(mcModId, Lists.newArrayList());
		
		//iterate all mob stats and add them to the map
		for(final SUMobStat mobStat : getMobStats(statsProvider, filter))
		{
			//---------- group the mob
			//obtain mod id
			final String entityModId = mobStat.getStatID().getNamespace();
			if(!result.containsKey(entityModId))
				result.put(entityModId, Lists.newArrayList());
			final Collection<SUMobStat> resultList = result.get(entityModId);
			
			//add the stat to the array
			resultList.add(mobStat);
		}
		
		//make sure 'minecraft' actually has entries
		//(aka handle cases where the filter filters out all 'minecraft' entries)
		if(result.get(mcModId).size() == 0)
			result.remove(mcModId);
		
		//return the result
		return result;
	}
	// --------------------------------------------------
	/**
	 * Same as {@link #getMobStatsByModGroups(IStatsProvider, Predicate)},
	 * but the {@link Map} keys represent {@link Component}ual names of the mods.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUMobStat}s.
	 */
	public static Map<Component, List<SUMobStat>> getMobStatsByModGroupsB
	(IStatsProvider statsProvider, @Nullable Predicate<SUMobStat> filter)
	{
		final var stats = getMobStatsByModGroups(statsProvider, filter);
		final var mapped = new LinkedHashMap<Component, List<SUMobStat>>();
		for(final var entry : stats.entrySet())
		{
			final var txt = entry.getKey() != null ? literal(TUtils.getModName(entry.getKey())) : literal("*");
			mapped.put(txt, entry.getValue());
		}
		return mapped;
	}
	// ==================================================
}