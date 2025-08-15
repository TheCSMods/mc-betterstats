package io.github.thecsdev.betterstats.client.gui.stats.panel.impl;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel.StatsTabPanelProxy;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;

final @Internal class StatsTabPanelProxyImpl implements StatsTabPanelProxy
{
	// ==================================================
	protected final BetterStatsPanel bsPanel;
	protected final BetterStatsPanelProxy proxy;
	// ==================================================
	public StatsTabPanelProxyImpl(BetterStatsPanel bsPanel) throws NullPointerException
	{
		this.bsPanel = Objects.requireNonNull(bsPanel);
		this.proxy = bsPanel.proxy;
	}
	// ==================================================
	public final @Override IStatsProvider getStatsProvider() { return this.proxy.getStatsProvider(); }
	public final @Override StatsTab getSelectedStatsTab() { return this.proxy.getSelectedStatsTab(); }
	public final @Override StatFilterSettings getFilterSettings() { return this.proxy.getFilterSettings(); }
	// ==================================================
}