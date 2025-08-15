package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.PlayerBadgeStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortCustomsBy;
import io.github.thecsdev.betterstats.api.util.stats.SUPlayerBadgeStat;
import io.github.thecsdev.tcdcommons.TCDCommons;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;

public final class PlayerBadgeStatsTab extends BSStatsTab<SUPlayerBadgeStat>
{
	// ==================================================
	public @Virtual @Override Component getName() { return translatable("tcdcommons.api.badge.playerbadge.plural"); }
	// --------------------------------------------------
	public final @Override boolean isAvailable() { return TCDCommons.getInstance().getConfig().enablePlayerBadges; }
	// ==================================================
	public final @Override void initStats(StatsInitContext initContext)
	{
		//gather initialization info and filter info
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		final var filters = initContext.getFilterSettings();
		final var predicate = getPredicate(initContext.getFilterSettings());
		
		final var filter_group = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_GROUP, FilterGroupBy.DEFAULT);
		final var filter_sort = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SORT_CUSTOMS, FilterSortCustomsBy.DEFAULT);
		
		//obtain stats and group/sort them
		Map<Component, List<SUPlayerBadgeStat>> statGroups = null;
		switch(filter_group)
		{
			case ALL:
				statGroups = new LinkedHashMap<>();
				statGroups.put(literal("*"), SUPlayerBadgeStat.getPlayerBadgeStats(stats, predicate));
				break;
			default:
				statGroups = SUPlayerBadgeStat.getPlayerBadgeStatsByModGroupsB(stats, predicate);
				break;
		}
		filter_sort.sortPlayerBadgeStats(statGroups);
		
		//initialize stats GUI
		for(final var statGroup : statGroups.entrySet())
		{
			final var group = statGroup.getKey();
			StatsTabUtils.initGroupLabel(panel, group != null ? group : literal("*"));
			initStats(panel, statGroup.getValue(), widget -> processWidget(widget));
		}
	}
	// --------------------------------------------------
	protected @Virtual @Override void initExtraFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initGroupByFilter(initContext);
		StatsTabUtils.initSortCustomsByFilter(initContext);
	}
	// --------------------------------------------------
	protected @Virtual Predicate<SUPlayerBadgeStat> getPredicate(StatFilterSettings filterSettings)
	{
		final String sq = filterSettings.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SEARCH, "");
		return stat -> stat.matchesSearchQuery(sq);
	}
	// ==================================================
	protected @Virtual void processWidget(PlayerBadgeStatWidget widget)
	{
		if(widget.getStat().value > 0) widget.setOutlineColor(BSStatsTabs.COLOR_SPECIAL);
		else if(!widget.getStat().isEmpty()) widget.setOutlineColor(TPanelElement.COLOR_OUTLINE);
	}
	// --------------------------------------------------
	public static @Internal void initStats(
			TPanelElement panel,
			Collection<SUPlayerBadgeStat> stats,
			Consumer<PlayerBadgeStatWidget> processWidget)
	{
		final int wmp = panel.getWidth() - (panel.getScrollPadding() * 2); //width minus padding
		int nextX = panel.getScrollPadding();
		int nextY = UILayout.nextChildBottomY(panel) - panel.getY();
		
		for(final SUPlayerBadgeStat stat : stats)
		{
			final var statElement = new PlayerBadgeStatWidget(nextX, nextY, stat);
			panel.addChild(statElement, true);
			if(processWidget != null)
				processWidget.accept(statElement);
			
			int SIZE = statElement.getWidth();
			nextX += SIZE + GAP;
			if(nextX + SIZE >= wmp)
			{
				nextX = panel.getScrollPadding();
				nextY = (UILayout.nextChildBottomY(panel) - panel.getY()) + GAP;
			}
		}
	}
	// ==================================================
}