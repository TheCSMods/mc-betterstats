package io.github.thecsdev.betterstats.client.gui.stats.panel;

import static io.github.thecsdev.betterstats.BetterStats.getModID;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.widget.ScrollBarWidget;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.StatsInitContext;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.StatFiltersPanelProxy;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TScrollBarWidget;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;

/**
 * Represents the {@link TPanelElement} where the statistics GUI is initialized.
 */
public final class StatsTabPanel extends BSComponentPanel
{
	// ==================================================
	public static final Component TXT_NO_STATS_YET = BST.sTab_noStatsYet();
	public static final ResourceLocation FILTER_ID_SCROLL_CACHE = ResourceLocation.fromNamespaceAndPath(getModID(), "stats_scroll_cache");
	// --------------------------------------------------
	protected final StatsTabPanelProxy proxy;
	// --------------------------------------------------
	protected @Nullable TPanelElement panel; //the statistics panel
	protected @Nullable TScrollBarWidget scroll_panel; //the vertical scroll-bar for the statistics panel
	// ==================================================
	public StatsTabPanel(int x, int y, int width, int height, StatsTabPanelProxy proxy) throws NullPointerException
	{
		super(x, y, width, height);
		this.proxy = Objects.requireNonNull(proxy);
	}
	// ==================================================
	/**
	 * Returns the {@link StatsTabPanelProxy} associated
	 * with this {@link StatsTabPanel}.
	 */
	public final StatsTabPanelProxy getProxy() { return this.proxy; }
	
	/**
	 * Used for caching {@link #FILTER_ID_SCROLL_CACHE}.
	 * @return A {@link Double} representing the statistics panel's current vertical scroll amount.
	 */
	@Internal
	public final double getVerticalScrollBarValue()
	{
		return (this.scroll_panel != null) ? this.scroll_panel.getValue() : 0;
	}
	// --------------------------------------------------
	protected final @Override void init()
	{
		final var fil = getProxy().getFilterSettings();
		
		//create and add the panel where the stats will be initialized
		this.panel = new TPanelElement(0, 0, getWidth() - 8, getHeight());
		this.panel.setScrollFlags(TPanelElement.SCROLL_VERTICAL);
		this.panel.setScrollPadding(10);
		this.panel.setSmoothScroll(BetterStats.getInstance().getConfig().guiSmoothScroll);
		this.panel.setBackgroundColor(0);
		this.panel.setOutlineColor(0);
		addChild(this.panel, true);
		
		//create a scroll-bar for the stats panel
		this.scroll_panel = new ScrollBarWidget(this.panel.getWidth(), 0, 8, this.panel.getHeight(), this.panel);
		addChild(this.scroll_panel, true);
		
		this.panel.eScrolledVertically.register((__, val) ->
			fil.setProperty(FILTER_ID_SCROLL_CACHE, this.scroll_panel.getValue()));
		
		//initialize the panel
		final StatsTab selTab = this.proxy.getSelectedStatsTab();
		if(selTab == null) return;
		final IStatsProvider statsProvider = this.proxy.getStatsProvider();
		final StatFilterSettings filterSettings = this.proxy.getFilterSettings();
		
		selTab.initStats(new StatsInitContext()
		{
			public IStatsProvider getStatsProvider() { return statsProvider; }
			public TPanelElement getStatsPanel() { return StatsTabPanel.this.panel; }
			public StatFilterSettings getFilterSettings() { return filterSettings; }
		});
		
		//handle cases where no GUI elements get added (no stats to show yet)
		if(this.panel.getChildren().size() == 0)
			init_noStatsLabel(this.panel);
		//handle scroll cache
		else this.scroll_panel.setValue(fil.getPropertyOrDefault(FILTER_ID_SCROLL_CACHE, 0D));
	}
	// --------------------------------------------------
	private final @Internal TLabelElement init_noStatsLabel(TPanelElement panel)
	{
		final int sp = panel.getScrollPadding();
		final var lbl = new TLabelElement(sp, sp, panel.getWidth() - (sp*2), panel.getHeight() - (sp*2));
		lbl.setText(TXT_NO_STATS_YET);
		lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		panel.addChild(lbl, true);
		return lbl;
	}
	// ==================================================
	/**
	 * A component that provides the {@link StatsTabPanel} with
	 * the necessary information to operate properly.
	 */
	public static interface StatsTabPanelProxy
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
		 * @see StatFiltersPanelProxy#getFilterSettings()
		 */
		public StatFilterSettings getFilterSettings();
	}
	// ==================================================
}