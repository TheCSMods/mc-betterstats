package io.github.thecsdev.betterstats.api.client.registry;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.FiltersInitContext;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.StatsInitContext;
import io.github.thecsdev.tcdcommons.api.registry.TMutableRegistry;
import io.github.thecsdev.tcdcommons.api.registry.TRegistry;

/**
 * {@link BetterStats} registries that are present on the client side only.
 */
public final class BSClientRegistries
{
	// ==================================================
	private BSClientRegistries() {}
	// ==================================================
	/**
	 * A {@link TRegistry} containing {@link StatsTab}s that will be
	 * shown on the list of "statistics tabs" to select from.
	 * @see StatsTab
	 * @see StatsTab#initStats(StatsInitContext)
	 * @see StatsTab#initFilters(FiltersInitContext)
	 */
	public static final TMutableRegistry<StatsTab> STATS_TAB = new TMutableRegistry<>();
	// ==================================================
}