package io.github.thecsdev.betterstats.api.client.registry;

import static io.github.thecsdev.betterstats.BetterStats.getModID;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.AdvancementsTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.BSConfigTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.BSCreditsTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.BSStatsSharingTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.FoodStuffsStatsTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.GeneralStatsTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.ItemStatsTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.MobStatsTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.MonstersHuntedStatsTab;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.PlayerBadgeStatsTab;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * {@link BetterStats}'s {@link StatsTab}s.
 */
public final class BSStatsTabs
{
	// ==================================================
	private BSStatsTabs() {}
	// ==================================================
	/**
	 * An {@link Integer} corresponding to the yellow color.<p>
	 * Used for group labels featured in {@link StatsTabUtils#initGroupLabel(TPanelElement, Component)}.
	 */
	public static final @Internal int COLOR_SPECIAL = 0xFFFFFF00;
	// --------------------------------------------------
	public static final StatsTab BSS_CREDITS       = new BSCreditsTab();
	public static final StatsTab BSS_CONFIG        = new BSConfigTab();
	public static final StatsTab GENERAL           = new GeneralStatsTab();
	public static final StatsTab ITEMS             = new ItemStatsTab();
	public static final StatsTab ENTITIES          = new MobStatsTab();
	public static final StatsTab FOOD_STUFFS       = new FoodStuffsStatsTab();
	public static final StatsTab MONSTERS_HUNTED   = new MonstersHuntedStatsTab();
	public static final StatsTab PLAYER_BADGES     = new PlayerBadgeStatsTab();
	public static final StatsTab BSS_STATS_SHARING = new BSStatsSharingTab();
	public static final StatsTab ADVANCEMENTS      = new AdvancementsTab();
	// ==================================================
	/**
	 * Registers the {@link BSStatsTabs} to the {@link BSClientRegistries#STATS_TAB} registry.
	 * @apiNote May only be called once.
	 */
	public static void register() {}
	static
	{
		final String modId = getModID();
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "bss_credits"),       BSS_CREDITS);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "bss_config"),        BSS_CONFIG);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "general"),           GENERAL);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "items"),             ITEMS);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "entities"),          ENTITIES);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "food_stuffs"),       FOOD_STUFFS);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "monsters_hunted"),   MONSTERS_HUNTED);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "player_badges"),     PLAYER_BADGES);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "bss_stats_sharing"), BSS_STATS_SHARING);
		BSClientRegistries.STATS_TAB.register(ResourceLocation.fromNamespaceAndPath(modId, "advancements"),      ADVANCEMENTS);
	}
	// ==================================================
}