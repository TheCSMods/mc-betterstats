package io.github.thecsdev.betterstats.api.client.gui.stats.panel;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUPlayerBadgeStat;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.PlayerBadgeStatsTab;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadge;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;

/**
 * Player badge summary panel.<br/>
 * Shows a few highlight {@link PlayerBadge}s for a given {@link IStatsProvider}.
 */
public final class PBSummaryPanel extends BSComponentPanel
{
	// ==================================================
	private final IStatsProvider statsProvider;
	// --------------------------------------------------
	protected int maxEntries = 10;
	// ==================================================
	public PBSummaryPanel(int x, int y, int width, int height, IStatsProvider statsProvider)
	{
		super(x, y, width, height);
		this.statsProvider = Objects.requireNonNull(statsProvider);
		
		this.setScrollPadding(5);
		this.setScrollFlags(TPanelElement.SCROLL_VERTICAL);
	}
	// ==================================================
	/**
	 * Returns the {@link IStatsProvider} associated with this {@link PBSummaryPanel}.
	 */
	public final IStatsProvider getStatsProvider() { return this.statsProvider; }
	// --------------------------------------------------
	public final int getMaxEntries() { return this.maxEntries; }
	public final void setMaxEntries(int maxEntries) { this.maxEntries = Math.abs(maxEntries); }
	// ==================================================
	protected final @Override void init()
	{
		//obtain stats, sorting highest to lowest
		final var stats = SUPlayerBadgeStat.getPlayerBadgeStats(this.statsProvider, s -> !s.isEmpty())
				.stream()
				.sorted(Comparator.<SUPlayerBadgeStat>comparingInt(entry -> entry.value).reversed())
				.limit(this.maxEntries)
				.collect(Collectors.toList());
		
		//place stats
		PlayerBadgeStatsTab.initStats(this, stats, w -> w.setSize(20, 20));
		
		//no stats label
		if(getChildren().size() == 0)
		{
			final var n1 = UILayout.nextChildVerticalRect(this);
			final var lbl = new TLabelElement(
					n1.x, getY() + (getHeight() / 2) - (n1.height / 2),
					n1.width, n1.height,
					StatsTabPanel.TXT_NO_STATS_YET);
			lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
			addChild(lbl, false);
		}
	}
	// ==================================================
}