package io.github.thecsdev.betterstats.api.util.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.badge.BSClientPlayerBadge;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadge;
import io.github.thecsdev.tcdcommons.api.registry.TRegistries;
import io.github.thecsdev.tcdcommons.api.util.TUtils;

public final class SUPlayerBadgeStat extends SUStat<PlayerBadge>
{
	// ==================================================
	protected final PlayerBadge playerBadge;
	protected final boolean isEmpty;
	// --------------------------------------------------
	/**
	 * Supposed to indicate the "quantity" of a given
	 * {@link PlayerBadge} that a given player has.
	 */
	public final int value;
	// ==================================================
	public SUPlayerBadgeStat(IStatsProvider statsProvider, PlayerBadge playerBadge) throws NullPointerException
	{
		super(statsProvider, Objects.requireNonNull(playerBadge.getId().orElse(null)), playerBadge.getName());
		this.playerBadge = playerBadge;
		
		//NOTE - Using client-side code in common environment. Surely nothing will go wrong here.
		//       Using `BetterStats.isClient()` before calling client code, to avoid issues.
		int clientSideAward = 0;
		if(BetterStats.isClient() && playerBadge instanceof BSClientPlayerBadge)
		{
			final var statCrit = ((BSClientPlayerBadge)playerBadge).getStatCriteria();
			clientSideAward = (statCrit != null) ? statCrit.apply(statsProvider) : 0;
		}
		
		this.value = statsProvider.getPlayerBadgeValue(getStatID()) + clientSideAward;
		this.isEmpty = (this.value == 0);
	}
	// ==================================================
	public final PlayerBadge getPlayerBadge() { return this.playerBadge; }
	// --------------------------------------------------
	public final @Override boolean isEmpty() { return this.isEmpty; }
	// ==================================================
	/**
	 * Obtains {@link PlayerBadge} stats, in form of {@link SUPlayerBadgeStat}s.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUPlayerBadgeStat}s.
	 */
	public static List<SUPlayerBadgeStat> getPlayerBadgeStats
	(IStatsProvider statsProvider, @Nullable Predicate<SUPlayerBadgeStat> filter)
	{
		//create the result list
		final var result = new ArrayList<SUPlayerBadgeStat>();
		
		//iterate all player badge types
		for(final var entry : TRegistries.PLAYER_BADGE)
		{
			//create the mob stat
			final var pbStat = new SUPlayerBadgeStat(statsProvider, entry.getValue());
			
			//filter
			if(filter != null && !filter.test(pbStat))
				continue;
			
			//add to the list
			result.add(pbStat);
		}
		
		//return the result
		return result;
	}
	
	/**
	 * Obtains {@link PlayerBadge} stats, in form of {@link SUPlayerBadgeStat}s, grouped
	 * into "mod groups" using a {@link Map}. The {@link Map} keys represent "mod IDs".
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUPlayerBadgeStat}s.
	 */
	public static Map<String, List<SUPlayerBadgeStat>> getPlayerBadgeStatsByModGroups
	(IStatsProvider statsProvider, @Nullable Predicate<SUPlayerBadgeStat> filter)
	{
		//create a new list
		final var result = new LinkedHashMap<String, List<SUPlayerBadgeStat>>();
		
		//add the 'minecraft' category first
		final String mcModId = ResourceLocation.DEFAULT_NAMESPACE;
		result.put(mcModId, Lists.newArrayList());
		
		//iterate all player badge stats and add them to the map
		for(final SUPlayerBadgeStat pbStat : getPlayerBadgeStats(statsProvider, filter))
		{
			//---------- group the mob
			//obtain mod id
			final String entityModId = pbStat.getStatID().getNamespace();
			if(!result.containsKey(entityModId))
				result.put(entityModId, Lists.newArrayList());
			final Collection<SUPlayerBadgeStat> resultList = result.get(entityModId);
			
			//add the stat to the array
			resultList.add(pbStat);
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
	 * Same as {@link #getPlayerBadgeStatsByModGroups(IStatsProvider, Predicate)},
	 * but the {@link Map} keys represent {@link Component}ual names of the mods.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUPlayerBadgeStat}s.
	 */
	public static Map<Component, List<SUPlayerBadgeStat>> getPlayerBadgeStatsByModGroupsB
	(IStatsProvider statsProvider, @Nullable Predicate<SUPlayerBadgeStat> filter)
	{
		final var stats = getPlayerBadgeStatsByModGroups(statsProvider, filter);
		final var mapped = new LinkedHashMap<Component, List<SUPlayerBadgeStat>>();
		for(final var entry : stats.entrySet())
		{
			final var txt = entry.getKey() != null ? literal(TUtils.getModName(entry.getKey())) : literal("*");
			mapped.put(txt, entry.getValue());
		}
		return mapped;
	}
	// ==================================================
}