package io.github.thecsdev.betterstats.client.gui.stats.panel.impl;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.gui.stats.panel.MenuBarPanel.MenuBarPanelProxy;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;

final @Internal class MenuBarPanelProxyImpl implements MenuBarPanelProxy
{
	// ==================================================
	protected final BetterStatsPanel bsPanel;
	protected final BetterStatsPanelProxy proxy;
	// ==================================================
	public MenuBarPanelProxyImpl(BetterStatsPanel bsPanel) throws NullPointerException
	{
		this.bsPanel = Objects.requireNonNull(bsPanel);
		this.proxy = bsPanel.proxy;
	}
	// ==================================================
	public final @Override IStatsProvider getStatsProvider() { return this.proxy.getStatsProvider(); }
	public final @Override void setSelectedStatsTab(StatsTab statsTab)
	{
		this.proxy.setSelectedStatsTab(statsTab);
		this.bsPanel.refresh();
	}
	// ==================================================
}