package io.github.thecsdev.betterstats.api.client.registry;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.gui.widget.SelectStatsTabWidget;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;

/**
 * Represents a "statistics tab" {@link Object} used in {@link StatsTabPanel}
 * for initializing a "statistics tab"'s GUI.
 * <p>
 * Examples include like the "General", "Items", and "Mobs" tabs.
 * 
 * @see StatsTab#initFilters(FiltersInitContext)
 * @see StatsTab#initStats(StatsInitContext)
 * 
 * @apiNote Register one using {@link BSClientRegistries#STATS_TAB}.
 */
public abstract class StatsTab extends Object
{
	// ==================================================
	/**
	 * Returns the name of this {@link StatsTab}.<br/>
	 * This {@link Component} will be rendered on the GUI to indicate the tab's name.
	 */
	public abstract Component getName();
	
	/**
	 * Invoked when a {@link StatsTabPanel} GUI is initializing for a given {@link StatsTab}.<p>
	 * Use this to initialize the {@link StatsTab}'s GUI.
	 * @param initContext The {@link StatsInitContext}.
	 */
	public abstract void initStats(StatsInitContext initContext);
	
	/**
	 * Invoked when a {@link StatFiltersPanel} GUI is initializing for a given {@link StatsTab}.<p>
	 * Use this to initialize the {@link StatFilterSettings} GUI.
	 * @param initContext The {@link FiltersInitContext}.
	 */
	public @Virtual void initFilters(FiltersInitContext initContext) { StatsTabUtils.initDefaultFilters(initContext); }
	
	/**
	 * Returns an {@link Optional} containing the unique {@link ResourceLocation}
	 * of this {@link StatsTab}. The {@link Optional} will contain {@code null}
	 * if this {@link StatsTab} isn't registered.
	 * @see BSClientRegistries#STATS_TAB
	 */
	public final Optional<ResourceLocation> getId() { return BSClientRegistries.STATS_TAB.getKey(this); }
	
	/**
	 * Returns {@code true} if this {@link StatsTab} should appear in the
	 * {@link SelectStatsTabWidget}'s dropdown menu at the time of the
	 * {@link SelectStatsTabWidget}'s creation.
	 * 
	 * @apiNote Changing the return value of this from {@code true} to
	 * {@code false} after this {@link StatsTab} was added to a
	 * {@link SelectStatsTabWidget}'s dropdown menu will not automatically
	 * remove it from the dropdown menu.
	 */
	public @Virtual boolean isAvailable() { return true; }
	// ==================================================
	/**
	 * The "initialization context" for when a {@link StatsTab}'s GUI is "initializing".
	 * @see StatsInitContext#getStatsPanel()
	 * @see StatsInitContext#getStatsProvider()
	 */
	public static interface StatsInitContext
	{
		/**
		 * Returns the {@link TPanelElement} onto which the GUI should be initialized.
		 * <p>
		 * Initialize all GUI onto this {@link TPanelElement}.
		 * 
		 * @apiNote Don't forget to take {@link TPanelElement#getScrollPadding()} into account!
		 * When adding {@link TElement}s, always make sure that padding is applied.
		 */
		public TPanelElement getStatsPanel();
		
		/**
		 * Returns the {@link IStatsProvider} that contains all the statistics.
		 * <p>
		 * Use this to obtain the statistics that should be shown on this {@link StatsTab}.
		 */
		public IStatsProvider getStatsProvider();
		
		/**
		 * Returns the filter settings/preferences set by the user.
		 */
		public StatFilterSettings getFilterSettings();
	}
	
	/**
	 * The "initialization context" for when a {@link StatsTab}'s corresponding "stat filters"
	 * GUI is "initializing". Use this to create your own stat filters GUI for this {@link StatsTab},
	 * and use {@link FiltersInitContext#getFilterSettings()} to obtain and store the filter settings.
	 * @see FiltersInitContext#getFiltersPanel()
	 * @see FiltersInitContext#getFilterSettings()
	 */
	public static interface FiltersInitContext
	{
		/**
		 * Returns the {@link TPanelElement} onto which the "filter" GUI elements should be initialized.
		 * Retrieve filter settings from {@link #getFilterSettings()}, as well as store them there.
		 * When the user makes a change to the filter settings, call {@link #refreshStatsTab()}.
		 */
		public TPanelElement getFiltersPanel();
		
		/**
		 * Returns the filter settings/preferences set by the user.
		 */
		public StatFilterSettings getFilterSettings();
		
		/**
		 * Call this whenever you wish to refresh the {@link StatsInitContext#getStatsPanel()}.
		 * @apiNote Do not make attempts to refresh it manually without calling this, please.
		 */
		public void refreshStatsTab();
		
		/**
		 * Returns the currently selected {@link StatsTab} that
		 * will be used to display player statistics.
		 */
		public StatsTab getSelectedStatsTab();
		
		/**
		 * @see BetterStatsPanelProxy#setSelectedStatsTab(StatsTab)
		 * @apiNote The {@link StatsTabPanel} is automatically refreshed here.
		 * @apiNote Do not call {@link #refreshStatsTab()} after calling this.
		 */
		public void setSelectedStatsTab(StatsTab statsTab);
	}
	// ==================================================
}