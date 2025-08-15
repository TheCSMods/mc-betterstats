package io.github.thecsdev.betterstats.client.gui.screen.hud;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IStatsListener;

@Internal class BetterStatsHudScreenWrapper extends TScreenWrapper<BetterStatsHudScreen> implements IStatsListener
{
	// ==================================================
	public BetterStatsHudScreenWrapper(BetterStatsHudScreen target) { super(target); }
	// --------------------------------------------------
	public final @Override void onStatsReady() { this.target.refresh(); }
	// ==================================================
}