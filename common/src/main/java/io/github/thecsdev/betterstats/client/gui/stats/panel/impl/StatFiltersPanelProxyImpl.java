package io.github.thecsdev.betterstats.client.gui.stats.panel.impl;

import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel.FILTER_ID_SCROLL_CACHE;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.StatFiltersPanelProxy;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;

final @Internal class StatFiltersPanelProxyImpl implements StatFiltersPanelProxy
{
	// ==================================================
	protected final BetterStatsPanel bsPanel;
	protected final BetterStatsPanelProxy proxy;
	// ==================================================
	public StatFiltersPanelProxyImpl(BetterStatsPanel bsPanel) throws NullPointerException
	{
		this.bsPanel = Objects.requireNonNull(bsPanel);
		this.proxy = bsPanel.proxy;
	}
	// ==================================================
	public final @Override StatFilterSettings getFilterSettings() { return this.proxy.getFilterSettings(); }
	public final @Override @Nullable StatsTab getSelectedStatsTab() { return this.proxy.getSelectedStatsTab(); }
	public final @Override void setSelectedStatsTab(StatsTab statsTab)
	{
		getFilterSettings().setProperty(FILTER_ID_SCROLL_CACHE, 0D); //clear scroll cache when switching tabs
		this.proxy.setSelectedStatsTab(statsTab);
	}
	public final @Override void refreshStatsTab()
	{
		getFilterSettings().setProperty(FILTER_ID_SCROLL_CACHE, 0D); //clear scroll cache when changing filter settings
		this.bsPanel.refreshStatsTab();
	}
	// ==================================================
}