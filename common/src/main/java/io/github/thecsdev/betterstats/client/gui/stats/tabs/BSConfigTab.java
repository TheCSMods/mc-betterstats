package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.util.TCDCT;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class BSConfigTab extends StatsTab
{
	// ==================================================
	public static final Component TEXT_TITLE = translatable("options.title");
	// --------------------------------------------------
	private @Nullable TConfigPanelBuilder<?> config_builder;
	// ==================================================
	public final @Override Component getName() { return TEXT_TITLE; }
	// --------------------------------------------------
	public final @Override boolean isAvailable() { return false; }
	// ==================================================
	public final @Override void initStats(StatsInitContext initContext)
	{
		//init config gui
		this.config_builder = initConfigGui(initContext.getStatsPanel());
	}
	// --------------------------------------------------
	public final @Override void initFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initDefaultFilters(initContext);
		
		final var panel = initContext.getFiltersPanel();
		StatsTabUtils.initGroupLabel(panel, TEXT_TITLE);
		
		final var n1 = UILayout.nextChildVerticalRect(panel);
		n1.y += 3;
		final var btn_done = new TButtonWidget(n1.x, n1.y, n1.width, 20, translatable("gui.done"));
		btn_done.setOnClick(__ ->
		{
			//this shouldn't happen:
			if(this.config_builder == null) return;
			
			//save and close
			this.config_builder.saveChanges();
			initContext.setSelectedStatsTab(BSStatsTabs.GENERAL);
			
			//refresh the BSP in the event gui-related settings were changed
			final @Nullable var bsp = (BetterStatsPanel)panel.findParent(p -> p instanceof BetterStatsPanel);
			if(bsp != null) bsp.refresh();
		});
		panel.addChild(btn_done, false);
		
		final var n2 = UILayout.nextChildVerticalRect(panel);
		n2.y += 3;
		final var btn_cancel = new TButtonWidget(n2.x, n2.y, n2.width, 20, translatable("gui.cancel"));
		btn_cancel.setOnClick(__ -> initContext.setSelectedStatsTab(BSStatsTabs.GENERAL));
		panel.addChild(btn_cancel, false);
	}
	// ==================================================
	/**
	 * Initializes the mod configuration GUI onto a {@link TPanelElement}.
	 * @apiNote Does NOT include the "Save" and "Cancel" buttons.
	 */
	public static final TConfigPanelBuilder<?> initConfigGui(TPanelElement panel)
	{
		//obtain the config reference, and create a config builder
		final var config         = BetterStats.getInstance().getConfig();
		final var configBuilder = TConfigPanelBuilder.builder(panel);
		
		initConfigGui_debug(config, configBuilder);
		initConfigGui_clientSide(config, configBuilder);
		initConfigGui_serverSide(config, configBuilder);
		
		//build
		configBuilder.build(() ->
		{
			try { config.saveToFile(true); }
			catch(Exception e) { throw new RuntimeException("Failed to save mod config.", e); }
		});
		
		//finally, once done, return the config builder instance
		return configBuilder;
	}
	
	private static final void initConfigGui_debug(BetterStatsConfig config, TConfigPanelBuilder<?> configBuilder)
	{
		//debug mode
		configBuilder.addCheckbox(
				BST.config_debugMode(),
				BetterStatsConfig.DEBUG_MODE,
				checkbox -> BetterStatsConfig.DEBUG_MODE = checkbox.getChecked());
	}
	
	private static final void initConfigGui_clientSide(BetterStatsConfig config, TConfigPanelBuilder<?> configBuilder)
	{
		//label
		configBuilder.addLabelB(TCDCT.tcdc_term_clientSide()).setTextColor(0xFFFFFF00);
		
		//gui smooth scroll
		configBuilder.addCheckbox(
				BST.config_guiSmoothScroll(),
				config.guiSmoothScroll,
				checkbox -> config.guiSmoothScroll = checkbox.getChecked());
		configBuilder.getLastAddedElement().setTooltip(Tooltip.create(BST.config_guiSmoothScroll_tooltip()));
		
		//gui mobs follow cursor
		configBuilder.addCheckbox(
					BST.config_guiMobsFollowCursor(),
					config.guiMobsFollowCursor,
					checkbox -> config.guiMobsFollowCursor = checkbox.getChecked());
		
		//trust all servers bss network
		configBuilder.addCheckbox(
					BST.config_trustAllServersBssNet(),
					config.trustAllServersBssNet,
					checkbox -> config.trustAllServersBssNet = checkbox.getChecked());
		configBuilder.getLastAddedElement().setTooltip(Tooltip.create(BST.config_trustAllServersBssNet_tooltip()));
		
		//allow stats sharing
		configBuilder.addCheckbox(
				BST.config_allowStatsSharing(),
				config.netPref_allowStatsSharing,
				checkbox -> config.netPref_allowStatsSharing = checkbox.getChecked());
		configBuilder.getLastAddedElement().setTooltip(Tooltip.create(BST.config_allowStatsSharing_tooltip()));
		
		//wide stats panel
		configBuilder.addCheckbox(
				BST.config_wideStatsPanel(),
				config.wideStatsPanel,
				checkbox -> config.wideStatsPanel = checkbox.getChecked());
		configBuilder.getLastAddedElement().setTooltip(Tooltip.create(BST.config_wideStatsPanel_tooltip()));
		
		//centered stats panel
		configBuilder.addCheckbox(
				BST.config_centeredStatsPanel(),
				config.centeredStatsPanel,
				checkbox -> config.centeredStatsPanel = checkbox.getChecked());
		configBuilder.getLastAddedElement().setTooltip(Tooltip.create(BST.config_centeredStatsPanel_tooltip()));
		
		//update (item groups / display context) on join
		configBuilder.addCheckbox(
				BST.config_updDspCtxOnJoin(),
				config.updateItemGroupsOnJoin,
				checkbox -> config.updateItemGroupsOnJoin = checkbox.getChecked());
		configBuilder.getLastAddedElement().setTooltip(Tooltip.create(BST.config_updDspCtxOnJoin_tooltip()));
		
		//hide player info
		configBuilder.addCheckbox(
				BST.config_hidePlayerInfo(),
				config.hidePlayerInfo,
				checkbox -> config.hidePlayerInfo = checkbox.getChecked());
		configBuilder.getLastAddedElement().setTooltip(Tooltip.create(BST.config_hidePlayerInfo_tooltip()));
	}
	
	private static final void initConfigGui_serverSide(BetterStatsConfig config, TConfigPanelBuilder<?> configBuilder)
	{
		//label
		configBuilder.addLabelB(TCDCT.tcdc_term_serverSide()).setTextColor(0xFFFFFF00);
		
		//register commands
		configBuilder.addCheckbox(
				BST.config_registerCommands(),
				config.registerCommands,
				checkbox -> config.registerCommands = checkbox.getChecked());
		
		//enable stat announcement system
		configBuilder.addCheckbox(
				BST.config_enableSas(),
				config.enableServerSAS,
				checkbox -> config.enableServerSAS = checkbox.getChecked());
	}
	// ==================================================
}