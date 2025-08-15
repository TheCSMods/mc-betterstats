package io.github.thecsdev.betterstats.client.gui.stats.panel;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.widget.ScrollBarWidget;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.FiltersInitContext;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;

public final class StatFiltersPanel extends BSComponentPanel
{
	// ==================================================
	public static final Component TXT_FILTERS = BST.filters();
	// --------------------------------------------------
	private final StatFiltersPanelProxy proxy;
	// ==================================================
	public StatFiltersPanel(int x, int y, int width, int height, StatFiltersPanelProxy proxy) throws NullPointerException
	{
		super(x, y, width, height);
		this.proxy = Objects.requireNonNull(proxy);
	}
	// ==================================================
	/**
	 * Returns the {@link StatFiltersPanelProxy} associated
	 * with this {@link StatFiltersPanel}.
	 */
	public final StatFiltersPanelProxy getProxy() { return this.proxy; }
	// --------------------------------------------------
	protected final @Override void init()
	{
		//create a panel for the filters
		final var panel = new TPanelElement(0, 0, getWidth() - 8, getHeight());
		panel.setScrollFlags(TPanelElement.SCROLL_VERTICAL);
		panel.setScrollPadding(10);
		panel.setSmoothScroll(true);
		panel.setBackgroundColor(0);
		panel.setOutlineColor(0);
		addChild(panel, true);
		
		//create a scroll bar for the panel
		final var scroll_panel = new ScrollBarWidget(panel.getWidth(), 0, 8, panel.getHeight(), panel);
		addChild(scroll_panel, true);
		
		//init filter gui
		final var statsTab = this.proxy.getSelectedStatsTab();
		if(statsTab == null) return; //safety check; should not be null tho
		final var filterSettings = this.proxy.getFilterSettings();
		
		statsTab.initFilters(new FiltersInitContext()
		{
			public TPanelElement getFiltersPanel() { return panel; }
			public StatFilterSettings getFilterSettings() { return filterSettings; }
			public void refreshStatsTab() { StatFiltersPanel.this.proxy.refreshStatsTab(); }
			public StatsTab getSelectedStatsTab() { return statsTab; }
			public void setSelectedStatsTab(StatsTab statsTab) { StatFiltersPanel.this.proxy.setSelectedStatsTab(statsTab); }
		});
	}
	// ==================================================
	/**
	 * A component that provides the {@link StatFiltersPanel} with
	 * the necessary information to operate properly.
	 */
	public static interface StatFiltersPanelProxy
	{
		/**
		 * Stores the "filter settings" aka user's stat filter
		 * preferences that apply for the current session.
		 * @apiNote Must not be {@code null}.
		 */
		public StatFilterSettings getFilterSettings();
		
		/**
		 * @see BetterStatsPanelProxy#getSelectedStatsTab()
		 */
		public StatsTab getSelectedStatsTab();
		
		/**
		 * @see BetterStatsPanelProxy#setSelectedStatsTab(StatsTab)
		 */
		public void setSelectedStatsTab(StatsTab statsTab);
		
		/**
		 * Refreshes the {@link StatsTabPanel}.
		 */
		public void refreshStatsTab();
	}
	// ==================================================
}