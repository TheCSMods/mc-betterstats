package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout.nextChildVerticalRect;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.stats.panel.StatsSummaryPanel;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.stats.SUStat;
import io.github.thecsdev.betterstats.client.gui.panel.PageChooserPanel;
import io.github.thecsdev.betterstats.client.gui.panel.PageChooserPanel.PageChooserPanelProxy;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;

/**
 * Contains {@link StatsTab}s that belong to {@link BetterStats}.
 */
@Internal abstract class BSStatsTab<S extends SUStat<?>> extends StatsTab
{
	// ==================================================
	@Internal BSStatsTab() {}
	// ==================================================
	public final @Override void initFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initDefaultFilters(initContext);
		StatsTabUtils.initShowEmptyStatsFilter(initContext);
		initExtraFilters(initContext);
	}
	protected @Virtual void initExtraFilters(FiltersInitContext initContext) {}
	// --------------------------------------------------
	protected @Virtual Predicate<S> getPredicate(StatFilterSettings filterSettings)
	{
		final String sq = filterSettings.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SEARCH, "");
		final boolean se = filterSettings.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SHOWEMPTY, false);
		return stat -> stat.matchesSearchQuery(sq) && (se || !stat.isEmpty());
	}
	// ==================================================
	/**
	 * Returns the {@link AtomicInteger} that represents the "page" filter value.
	 * @param filters The {@link StatFilterSettings}.
	 */
	protected final AtomicInteger getPageFilter(StatFilterSettings filters)
	{
		final var fid = ResourceLocation.fromNamespaceAndPath(
				getModID(),
				getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace('$', '.') + "_page");
		final var f = filters.getPropertyOrDefault(fid, new AtomicInteger(1));
		filters.setProperty(fid, f); //set the value if absent, which it will be initially
		return f;
	}
	// --------------------------------------------------
	/**
	 * Initializes a {@link StatsSummaryPanel} onto a {@link TPanelElement}.
	 * @param panel The {@link TPanelElement} to initialize onto.
	 */
	@Internal static @Nullable StatsSummaryPanel initStatsSummary(TPanelElement panel)
	{
		//do not summarize if no children are present
		if(panel.getChildren().size() < 1) return null;
		
		//summary group label
		final var lbl = StatsTabUtils.initGroupLabel(panel, literal("\u2190 \u2022 \u2192"));
		lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//init summary panel
		final var n1 = nextChildVerticalRect(panel);
		final var summary = new StatsSummaryPanel(n1.x, n1.y + GAP, n1.width);
		panel.addChild(summary, false);
		return summary;
	}
	// --------------------------------------------------
	/**
	 * Initializes a {@link PageChooserPanel}.
	 * @param initContext The {@link StatsInitContext}.
	 * @param totalItemCount The total number of items present.
	 * @param itemsPerPage The maximum number of items to be displayed per page.
	 */
	protected @Virtual void initPageChooser(StatsInitContext initContext, int totalItemCount, int itemsPerPage)
	{
		//obtain page filter
		final var filter_page = getPageFilter(initContext.getFilterSettings());
		final var filter_maxPages = Math.max((int)Math.ceil((double)totalItemCount / Math.max(itemsPerPage, 1)), 1);
		
		//obtain the panel, and calculate the next XYWH
		final var panel = initContext.getStatsPanel();
		final var n1 = nextChildVerticalRect(panel);
		if(panel.getChildren().size() != 0) n1.y += GAP;
		
		//define logic for triggering a manual refresh
		final Runnable triggerRefresh = () -> { panel.clearChildren(); initStats(initContext); };
		
		//create and add the page chooser panel
		final var pc = new PageChooserPanel(n1.x, n1.y, n1.width, new PageChooserPanelProxy()
		{
			public int getPage() { return filter_page.get(); }
			public int getPageCount() { return filter_maxPages; }
			public void setPage(int page) { filter_page.set(page); triggerRefresh.run(); }
		});
		panel.addChild(pc, false);
	}
	// ==================================================
}