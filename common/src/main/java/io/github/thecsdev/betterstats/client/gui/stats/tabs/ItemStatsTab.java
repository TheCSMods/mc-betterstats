package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.SIZE;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.TUtils.safeSubList;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortItemsBy;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.screen.hud.entry.StatsHudItemEntry;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;

public @Internal @Virtual class ItemStatsTab extends BSStatsTab<SUItemStat>
{
	// ==================================================
	private static final int ITEMS_PER_PAGE = 700;
	// ==================================================
	public @Virtual @Override Component getName() { return translatable("stat.itemsButton"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		//gather initialization info and filter info
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		final var filters      = initContext.getFilterSettings();
		final var filter_group = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_GROUP, FilterGroupBy.DEFAULT);
		final var filter_sort  = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SORT_ITEMS, FilterSortItemsBy.DEFAULT);
		
		//obtain stats and sort them
		final var itemStats = SUItemStat.getItemStats(stats, getPredicate(filters));
		final int itemStatsSize = itemStats.size();
		filter_sort.sortItemStats(itemStats);
		
		//initialize stats GUI
		if(itemStatsSize > 0)
		{
			//top page chooser
			initPageChooser(initContext, itemStatsSize, ITEMS_PER_PAGE);
			
			//item stats, grouped
			{
				//paginate items
				final int maxPages = Math.max((int)Math.ceil((double)itemStatsSize / ITEMS_PER_PAGE), 1);
				final int page     = Math.min(getPageFilter(filters).get(), maxPages);
				final int from = Math.max(page - 1, 0) * ITEMS_PER_PAGE;
				final int to   = Math.max(Math.min(page * ITEMS_PER_PAGE, itemStatsSize), from);
				final var subl = safeSubList(itemStats, from, to);
				
				//group the paginated items
				final Map<Component, List<SUItemStat>> statGroups = (filter_group == FilterGroupBy.DEFAULT) ?
						getDefaultGroupFilter().apply(subl) : filter_group.apply(subl);
				
				//init gui for each group
				for(final var statGroup : statGroups.entrySet())
				{
					final var group = statGroup.getKey();
					StatsTabUtils.initGroupLabel(panel, group != null ? group : literal("*"));
					initStats(panel, statGroup.getValue(), widget -> processWidget(widget));
				}
			}
			
			//stats summary
			final var summary = initStatsSummary(panel);
			summary.summarizeItemStats(itemStats);
			summary.autoHeight();
			
			//bottom page chooser
			initPageChooser(initContext, itemStatsSize, ITEMS_PER_PAGE);
		}
	}
	// --------------------------------------------------
	protected @Virtual @Override void initExtraFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initGroupByFilter(initContext);
		StatsTabUtils.initSortItemsByFilter(initContext);
	}
	// ==================================================
	/**
	 * Returns the {@link FilterGroupBy} that'll be used by "default".
	 * @apiNote Must not return {@code null}.
	 */
	protected @Virtual FilterGroupBy getDefaultGroupFilter() { return FilterGroupBy.DEFAULT; }
	// --------------------------------------------------
	/**
	 * Initializes a {@link Collection} of {@link SUItemStat} onto a {@link TPanelElement}.
	 * @param panel The {@link TPanelElement}.
	 * @param stats The {@link SUItemStat}s to initialize.
	 * @param processWidget Optional {@link Consumer} that allows you to make changes to widgets as they are created.
	 */
	protected @Virtual void initStats(
			TPanelElement panel,
			Collection<SUItemStat> stats,
			Consumer<ItemStatWidget> processWidget)
	{
		final int wmp = panel.getWidth() - (panel.getScrollPadding() * 2); //width minus padding
		int nextX = panel.getScrollPadding();
		int nextY = UILayout.nextChildBottomY(panel) - panel.getY();
		
		for(final SUItemStat stat : stats)
		{
			final var statElement = new ItemStatWidget(nextX, nextY, stat);
			panel.addChild(statElement, true);
			if(processWidget != null)
				processWidget.accept(statElement);
			
			nextX += SIZE + GAP;
			if(nextX + SIZE >= wmp)
			{
				nextX = panel.getScrollPadding();
				nextY = (UILayout.nextChildBottomY(panel) - panel.getY()) + GAP;
			}
		}
	}
	//
	/**
	 * Default {@link ItemStatWidget} processing logic.<br/>
	 * Primarily used for {@link #initStats(TPanelElement, Collection, Consumer)}.
	 * @param widget The {@link ItemStatWidget}.
	 */
	protected @Virtual void processWidget(ItemStatWidget widget)
	{
		widget.eContextMenu.register((__, cMenu) ->
		{
			//do not add the "pin to hud" button if viewing third-party stats
			//(because pinning 3rd party stats is not supported yet...)
			if(!(widget.getStat().getStatsProvider() instanceof LocalPlayerStatsProvider))
				return;
			
			//continue as usual otherwise...
			cMenu.addButton(BST.hud_pinStat(), ___ ->
			{
				final var hud = BetterStatsHudScreen.getInstance();
				hud.setParentScreen(MC_CLIENT.screen);
				hud.addEntry(new StatsHudItemEntry(widget.getStat()));
				MC_CLIENT.setScreen(hud.getAsScreen());
			});
			cMenu.addButton(translatable("mco.selectServer.close"), ___ -> {});
		});
	}
	// ==================================================
}