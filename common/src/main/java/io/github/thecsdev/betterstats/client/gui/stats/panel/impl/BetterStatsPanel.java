package io.github.thecsdev.betterstats.client.gui.stats.panel.impl;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.gui.stats.panel.ActionBarPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.ActionBarPanel.ActionBarPanelProxy;
import io.github.thecsdev.betterstats.client.gui.stats.panel.MenuBarPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.StatFiltersPanelProxy;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel;

/**
 * A {@link BSComponentPanel} that usually goes on the {@link BetterStatsScreen}.<p>
 * It displays player statistics, alongside other components such as the
 * {@link StatFiltersPanel} and {@link MenuBarPanel}.
 */
public final class BetterStatsPanel extends BSComponentPanel
{
	// ==================================================
	protected final BetterStatsPanelProxy proxy;
	// --------------------------------------------------
	protected @Nullable MenuBarPanel panel_menu;
	protected @Nullable StatFiltersPanel panel_filters;
	protected @Nullable ActionBarPanel panel_actionBar;
	protected @Nullable StatsTabPanel panel_stats;
	// ==================================================
	public BetterStatsPanel(int x, int y, int width, int height, BetterStatsPanelProxy proxy)
	{
		super(x, y, Math.max(width, 100), Math.max(height, 100));
		this.outlineColor = 0;
		this.backgroundColor = 0; //as of 1.20.5, there's a background blur anyways..
		
		this.proxy = Objects.requireNonNull(proxy);
	}
	// ==================================================
	/**
	 * Refreshes the {@link StatsTabPanel}.
	 * @apiNote If this panel is not yet initialized, nothing will happen.
	 */
	public final void refreshStatsTab() { if(this.panel_stats != null) this.panel_stats.refresh(); }
	
	/**
	 * Returns {@link StatsTabPanel#getVerticalScrollBarValue()}.
	 */
	@Internal
	public final double getStatsTabVerticalScrollAmount()
	{
		return (this.panel_stats != null) ? this.panel_stats.getVerticalScrollBarValue() : 0;
	}
	// ==================================================
	protected final @Override void init()
	{
		//calculate dimensions and stuff
		final int gap = 4;
		
		//calculations
		final var config = BetterStats.getInstance().getConfig();
		int rootX = getWidth() / 18;
		int rootY = 0;
		int rootW = getWidth() - (getWidth() / 9);
		int rootH = getHeight() - gap;
		
		if(config.centeredStatsPanel) { rootH -= gap; rootY = gap; }
		if(config.wideStatsPanel)
		{
			rootX = 0; rootW = getWidth();
			rootY = 0; rootH = getHeight();
		}
		
		//the "root" panel
		final var root = new TPanelElement(rootX, rootY, rootW, rootH);
		root.setScrollFlags(0);
		root.setScrollPadding(0);
		root.setBackgroundColor(0);
		root.setOutlineColor(0);
		addChild(root, false);
		
		//add panels
		this.panel_menu = new MenuBarPanel(0, 0, root.getWidth(), new MenuBarPanelProxyImpl(this));
		root.addChild(this.panel_menu, true);
		
		this.panel_filters = new StatFiltersPanel(
				0,
				MenuBarPanel.HEIGHT + gap,
				(root.getWidth() / 3) - gap,
				root.getHeight() - (MenuBarPanel.HEIGHT + gap) - (ActionBarPanel.HEIGHT + gap),
				new StatFiltersPanelProxyImpl(this));
		root.addChild(this.panel_filters, true);
		
		this.panel_actionBar = new ActionBarPanel(
				this.panel_filters.getX(),
				this.panel_filters.getEndY() + gap,
				this.panel_filters.getWidth(),
				new ActionBarPanelProxy()
				{
					public void setSelectedStatsTab(StatsTab statsTab) { BetterStatsPanel.this.proxy.setSelectedStatsTab(statsTab); }
				});
		root.addChild(this.panel_actionBar, false);
		
		this.panel_stats = new StatsTabPanel(
				this.panel_filters.getEndX() + gap,
				this.panel_menu.getEndY() + gap,
				root.getWidth() - (this.panel_filters.getWidth() + 5),
				root.getHeight() - (this.panel_menu.getHeight() + gap),
				new StatsTabPanelProxyImpl(this));
		root.addChild(this.panel_stats, false);
	}
	// ==================================================
	/**
	 * A component that provides the {@link BetterStatsPanel} with
	 * the necessary information to operate properly.
	 */
	public static interface BetterStatsPanelProxy
	{
		/**
		 * Returns the {@link IStatsProvider} that will be used
		 * to visually display the player statistics.
		 */
		public IStatsProvider getStatsProvider();
		
		/**
		 * Returns the currently selected {@link StatsTab} that
		 * will be used to display player statistics.
		 */
		public StatsTab getSelectedStatsTab();
		
		/**
		 * Sets the currently selected {@link StatsTab} that
		 * will be used to display player statistics.
		 * @param statsTab The new {@link StatsTab}.
		 * 
		 * @apiNote Do not call {@link BetterStatsPanel#refresh()} here,
		 * as it will be done automatically.
		 */
		public void setSelectedStatsTab(StatsTab statsTab);
		
		/**
		 * @see StatFiltersPanelProxy#getFilterSettings()
		 * @apiNote Must not be {@code null}.
		 */
		public StatFilterSettings getFilterSettings();
	}
	// ==================================================
}