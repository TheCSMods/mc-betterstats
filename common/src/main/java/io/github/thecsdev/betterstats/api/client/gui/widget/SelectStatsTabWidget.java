package io.github.thecsdev.betterstats.api.client.gui.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.NoSuchElementException;
import java.util.Objects;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.client.registry.BSClientRegistries;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TSelectWidget;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;

/**
 * A {@link TSelectWidget} for {@link StatsTab}s.<br/>
 * Allows the user to Select a {@link StatsTab} via a GUI interface.
 */
public @Virtual class SelectStatsTabWidget extends TSelectWidget<SelectStatsTabWidget.StatsTabEntry>
{
	// ==================================================
	public SelectStatsTabWidget(int x, int y, int width, int height) { this(x, y, width, height, DEFAULT_LABEL); }
	public SelectStatsTabWidget(int x, int y, int width, int height, Component text)
	{
		//super
		super(x, y, width, height, text);
		
		//add enum entries using the enum type
		final boolean debug = BetterStatsConfig.DEBUG_MODE;
		for(final var statsTabRegEntry : BSClientRegistries.STATS_TAB)
		{
			//null check and availability check
			final StatsTab statsTab = statsTabRegEntry.getValue();
			if(statsTab == null || !statsTab.isAvailable())
				continue;
			
			//create and add entry
			final StatsTabEntry entry = new StatsTabEntry(statsTab);
			entry.tooltip = (debug ? Tooltip.create(literal(Objects.toString(statsTabRegEntry.getKey()))) : null);
			addEntry(entry);
		}
	}
	// ==================================================
	/**
	 * Returns a {@link StatsTabEntry} that is associated with a given {@link StatsTab} value.
	 * Will return {@code null} if no such {@link StatsTabEntry} exists or was removed.
	 */
	public final StatsTabEntry entryOf(StatsTab statsTab) { return this.entries.find(e -> e.getStatsTab() == statsTab); }
	// --------------------------------------------------
	/**
	 * Sets the selected {@link StatsTabEntry} using its {@link StatsTab} value.
	 * @throws NoSuchElementException If this {@link SelectStatsTabWidget} does not have
	 * a {@link StatsTabEntry} that corresponds with the given {@link StatsTab} value.
	 * @see #entryOf(StatsTab)
	 * @see StatsTabEntry#getStatsTab()
	 */
	public final void setSelected(StatsTab statsTab) throws NoSuchElementException
	{
		final var e = entryOf(statsTab);
		if(e == null && statsTab != null)
			throw new NoSuchElementException();
		setSelected(e);
	}
	// ==================================================
	public static final class StatsTabEntry implements TSelectWidget.Entry
	{
		// ----------------------------------------------
		protected final StatsTab statsTab;
		protected @Nullable Tooltip tooltip;
		// ----------------------------------------------
		public StatsTabEntry(StatsTab statsTab) throws NullPointerException
		{
			this.statsTab = Objects.requireNonNull(statsTab);
		}
		// ----------------------------------------------
		public final StatsTab getStatsTab() { return this.statsTab; }
		// ----------------------------------------------
		public final @Override Component getText() { return this.statsTab.getName(); }
		public final @Override @Nullable Runnable getOnSelect() { return null; }
		public final @Override @Nullable Tooltip getTooltip() { return this.tooltip; }
		// ----------------------------------------------
	}
	// ==================================================
}