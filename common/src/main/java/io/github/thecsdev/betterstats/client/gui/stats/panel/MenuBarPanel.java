package io.github.thecsdev.betterstats.client.gui.stats.panel;

import static io.github.thecsdev.betterstats.BetterStatsProperties.URL_CURSEFORGE;
import static io.github.thecsdev.betterstats.BetterStatsProperties.URL_ISSUES;
import static io.github.thecsdev.betterstats.BetterStatsProperties.URL_MODRINTH;
import static io.github.thecsdev.betterstats.BetterStatsProperties.URL_SOURCES;
import static io.github.thecsdev.betterstats.BetterStatsProperties.URL_WEBSITE;
import static io.github.thecsdev.betterstats.BetterStatsProperties.URL_YOUTUBE;
import static io.github.thecsdev.betterstats.api.client.registry.BSClientRegistries.STATS_TAB;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.TCDCommonsConfig.RESTRICTED_MODE;
import static io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils.showUrlPrompt;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Map.Entry;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.events.client.gui.BetterStatsGUIEvent;
import io.github.thecsdev.betterstats.api.util.io.IEditableStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.StatsProviderIO;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.menu.TContextMenuPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.menu.TMenuBarPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TStackTraceScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.explorer.TFileChooserResult.ReturnValue;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.explorer.TFileChooserScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;

/**
 * Represents the "menu bar" GUI component that usually shows
 * up on the top of the {@link BetterStatsScreen}.
 */
public final class MenuBarPanel extends BSComponentPanel
{
	// ==================================================
	/**
	 * The default {@link MenuBarPanel}'s {@link #getHeight()}.
	 */
	public static final int HEIGHT = TMenuBarPanel.HEIGHT;
	// --------------------------------------------------
	private final MenuBarPanelProxy proxy;
	// ==================================================
	public MenuBarPanel(int x, int y, int width, MenuBarPanelProxy proxy) throws NullPointerException
	{
		super(x, y, width, HEIGHT);
		this.proxy = Objects.requireNonNull(proxy);
	}
	// --------------------------------------------------
	public final @Override void setSize(int width, int height, int flags)
	{
		//set size as usual
		super.setSize(width, height, flags);
		//if invoking event handlers, then also refresh this panel
		if(getParentTScreen() != null && (flags & SS_INVOKE_EVENT) == SS_INVOKE_EVENT)
			refresh();
	}
	// ==================================================
	/**
	 * Returns the {@link MenuBarPanelProxy} associated
	 * with this {@link MenuBarPanel}.
	 */
	public final MenuBarPanelProxy getProxy() { return this.proxy; }
	// ==================================================
	protected final @Override void init()
	{
		//pre-init stuff
		final var localPlayer = MC_CLIENT.player;
		
		//create the menu bar panel
		final var menu = new TMenuBarPanel(0, 0, getWidth());
		menu.setBackgroundColor(0); //clear colors because
		menu.setOutlineColor(0);    //this panel already has them
		
		//add menu item elements
		menu.addButton(BST.menu_file(), btn ->
		{
			final var cMenu = new TContextMenuPanel(btn);
			//cMenu.addButton(translatable(tr + "menu_file.new"), null);
			cMenu.addButton(BST.menu_file_open(), __ ->
			{
				TFileChooserScreen.showOpenFileDialog(StatsProviderIO.FILE_EXTENSION, result ->
				{
					if(result.getReturnValue() != ReturnValue.APPROVE_OPTION)
						return;
					try
					{
						//show the stats screen
						final var stats = StatsProviderIO.loadFromFile(result.getSelectedFile());
						final var parentScreen = GuiUtils.getCurrentScreenParent();
						MC_CLIENT.setScreen(new BetterStatsScreen(parentScreen, stats).getAsScreen());
					}
					catch(Exception exc) { MC_CLIENT.setScreen(new TStackTraceScreen(MC_CLIENT.screen, exc).getAsScreen()); }
				});
			});
			//cMenu.addButton(translatable(tr + "menu_file.save"), null);
			cMenu.addButton(BST.menu_file_saveAs(), __ ->
			{
				TFileChooserScreen.showSaveFileDialog(StatsProviderIO.FILE_EXTENSION, result ->
				{
					if(result.getReturnValue() != ReturnValue.APPROVE_OPTION)
						return;
					try
					{
						final var file = result.getSelectedFile();
						final var stats = MenuBarPanel.this.proxy.getStatsProvider();
						StatsProviderIO.saveToFile(file, stats);
					}
					catch(Exception exc) { MC_CLIENT.setScreen(new TStackTraceScreen(MC_CLIENT.screen, exc).getAsScreen()); }
				});
			});
			cMenu.open();
		});
		
		menu.addButton(BST.menu_view(), btn ->
		{
			//create the context menu
			final var cMenu = new TContextMenuPanel(btn);
			
			//vanilla stats
			cMenu.addButton(BST.menu_view_vStats(), __ ->
			{
				if(localPlayer == null) return;
				MC_CLIENT.setScreen(new StatsScreen(
						MC_CLIENT.screen,
						localPlayer.getStats()));
			});
			
			//statistics hud
			cMenu.addButton(BetterStatsHudScreen.TEXT_TITLE, __ ->
			{
				final var sc = BetterStatsHudScreen.getInstance();
				sc.setParentScreen(MC_CLIENT.screen);
				MC_CLIENT.setScreen(sc.getAsScreen());
			});
			
			//stats tab entries
			cMenu.addSeparator();
			for(final Entry<ResourceLocation, StatsTab> statsTabEntry : STATS_TAB)
			{
				final StatsTab statsTab = statsTabEntry.getValue();
				if(!statsTab.isAvailable()) continue; //only show it if available
				cMenu.addButton(statsTab.getName(), __ -> this.proxy.setSelectedStatsTab(statsTab));
			}
			
			//options and credits
			cMenu.addSeparator();
			cMenu.addButton(translatable("options.title"), __-> this.proxy.setSelectedStatsTab(BSStatsTabs.BSS_CONFIG));
			cMenu.addButton(translatable("credits_and_attribution.button.credits"), __->
				this.proxy.setSelectedStatsTab(BSStatsTabs.BSS_CREDITS));
			
			//open the context menu
			cMenu.open();
		});
		
		menu.addButton(BST.menu_about(), btn ->
		{
			//create the context menu
			final var cMenu = new TContextMenuPanel(btn);
			
			//url-s
			cMenu.addButton(BST.menu_about_src(), __ -> showUrlPrompt(URL_SOURCES, true));
			cMenu.addButton(translatable("menu.reportBugs"), __ -> showUrlPrompt(URL_ISSUES, true));
			cMenu.addSeparator();
			cMenu.addButton(BST.menu_about_cf(), __ -> showUrlPrompt(URL_CURSEFORGE, true));
			cMenu.addButton(BST.menu_about_mr(), __ -> showUrlPrompt(URL_MODRINTH, true));
			if(!RESTRICTED_MODE)
			{
				cMenu.addSeparator();
				cMenu.addButton(BST.menu_about_website(), __ -> showUrlPrompt(URL_WEBSITE, true));
				cMenu.addButton(BST.menu_about_yt(), __ -> showUrlPrompt(URL_YOUTUBE, true));
				//cMenu.addButton(BST.menu_about_kofi(), __ -> showUrlPrompt(URL_KOFI, true));
				//cMenu.addButton(BST.menu_about_dc(), __ -> showUrlPrompt(URL_DISCORD, true));
			}
			
			cMenu.addSeparator();
			cMenu.addButton(translatable("credits_and_attribution.button.credits"), __->
				this.proxy.setSelectedStatsTab(BSStatsTabs.BSS_CREDITS));
			
			//open the context menu
			cMenu.open();
		});
		
		menu.addButton(
				BSStatsTabs.BSS_STATS_SHARING.getName(),
				btn -> this.proxy.setSelectedStatsTab(BSStatsTabs.BSS_STATS_SHARING))
			.setTooltip(Tooltip.create(literal("")
				.append(BST.gui_tpsbs()).append("\n")
				.append(BST.gui_tpsbs_description().withStyle(ChatFormatting.GRAY))
			));
		
		//invoke the menu bar event here, so other mods can add their options
		BetterStatsGUIEvent.MENU_BAR_INITIALIZED.invoker().invoke(menu);
		
		//add the menu bar panel
		addChild(menu, true);
		
		//add the display name label
		if(!BetterStats.getInstance().getConfig().hidePlayerInfo)
		{
			final var statsProvider = this.proxy.getStatsProvider();
			final boolean isESP = (statsProvider instanceof IEditableStatsProvider);
			
			@Nullable Component displayName = (statsProvider != null) ? statsProvider.getDisplayName() : null;
			if(displayName == null) displayName = literal("-"); //both conditions above can return null
			
			final int displayNameW = getTextRenderer().width(displayName);
			
			final int menuSP = menu.getScrollPadding();
			final var lbl_displayName = new TLabelElement(
					menu.getEndX() - (menuSP + displayNameW + getTextRenderer().lineHeight), menu.getY() + menuSP,
					displayNameW, menu.getHeight() - (menuSP*2),
					displayName);
			lbl_displayName.setTextColor(isESP ? 0xFFFFFFFF : BSStatsTabs.COLOR_SPECIAL);
			addChild(lbl_displayName, false);
		}
	}
	// ==================================================
	/**
	 * A component that provides the {@link MenuBarPanel} with
	 * the necessary information to operate properly.
	 */
	public static interface MenuBarPanelProxy
	{
		/**
		 * @see BetterStatsPanelProxy#getStatsProvider()
		 */
		public IStatsProvider getStatsProvider();
		
		/**
		 * @see BetterStatsPanelProxy#setSelectedStatsTab(StatsTab)
		 */
		public void setSelectedStatsTab(StatsTab statsTab);
	}
	// ==================================================
}