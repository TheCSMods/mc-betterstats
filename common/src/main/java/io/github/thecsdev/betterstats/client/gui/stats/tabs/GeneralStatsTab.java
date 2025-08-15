package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.FILTER_ID_FORMAT_DISTANCE;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.FILTER_ID_FORMAT_TIME;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.FILTER_ID_SORT_CUSTOMS;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel.TXT_NO_STATS_YET;
import static io.github.thecsdev.tcdcommons.api.hooks.world.biome.source.BiomeAccessHooks.getBiomeAccessSeed;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.StatFormatter;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.client.gui.stats.panel.GameProfilePanel;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortCustomsBy;
import io.github.thecsdev.betterstats.api.util.formatters.StatValueFormatter;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.screen.hud.entry.StatsHudGeneralEntry;
import io.github.thecsdev.betterstats.mixin.hooks.AccessorStat;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TFillColorElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;

public final @Internal class GeneralStatsTab extends BSStatsTab<SUGeneralStat>
{
	// ==================================================
	public final @Override Component getName() { return translatable("stat.generalButton"); }
	// --------------------------------------------------
	public final @Override void initStats(final StatsInitContext initContext)
	{
		//obtain initialization info
		final var panel = initContext.getStatsPanel();
		final int sp = panel.getScrollPadding();
		
		final var statsProvider = initContext.getStatsProvider();
		final var filterSettings = initContext.getFilterSettings();
		
		//obtain statistics and sort them
		final var groupBy = filterSettings.getPropertyOrDefault(StatsTabUtils.FILTER_ID_GROUP, FilterGroupBy.DEFAULT);
		final var sortBy = filterSettings.getProperty(FILTER_ID_SORT_CUSTOMS, FilterSortCustomsBy.DEFAULT);
		
		final var timeFormat = filterSettings.getPropertyOrDefault(FILTER_ID_FORMAT_TIME, StatValueFormatter.TIME);
		final var distanceFormat = filterSettings.getPropertyOrDefault(FILTER_ID_FORMAT_DISTANCE, StatValueFormatter.DISTANCE);
		
		// ---------- initialize gui
		//game profile panel
		if(!BetterStats.getInstance().getConfig().hidePlayerInfo)
		{
			final var panel_gp = new GameProfilePanel(sp, sp, panel.getWidth() - (sp*2), statsProvider);
			panel.addChild(panel_gp, true);
		}
		
		// ---------- "debug mode"-specific statistics
		if(BetterStatsConfig.DEBUG_MODE && statsProvider instanceof LocalPlayerStatsProvider)
			initDebugInfo(panel); 
		
		// ---------- general statistics
		//player stats label
		if(BetterStatsConfig.DEBUG_MODE)
			StatsTabUtils.initGroupLabel(panel, translatable("entity.minecraft.player"))
				.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//obtain and sort general stats
		final var statGroups = (groupBy == FilterGroupBy.DEFAULT) ?
				getDefaultGroupFilter().apply(SUGeneralStat.getGeneralStats(statsProvider, getPredicate(filterSettings))) :
				groupBy.apply(SUGeneralStat.getGeneralStats(statsProvider, getPredicate(filterSettings)));
		statGroups.entrySet().forEach(e -> sortBy.sortGeneralStats(e.getValue()));
		
		//init general stats
		if(statGroups.size() > 0) for(final var statGroup : statGroups.entrySet())
		{
			final var group = statGroup.getKey();
			StatsTabUtils.initGroupLabel(panel, group != null ? group : literal("*"));
			initStats(panel, statGroup.getValue(), widget ->
			{
				//process the widget, as usual
				processWidget(widget);
				
				//experimental action going on here (v3.13)...
				final var statFormat = ((AccessorStat)widget.getStat().getGeneralStat()).getFormatter();
				if(statFormat == StatFormatter.TIME) widget.setFormatter(timeFormat);
				else if(statFormat == StatFormatter.DISTANCE) widget.setFormatter(distanceFormat);
			});
		}
		else //init "no stats" label
		{
			//obtain the next XYWH
			final var n1 = UILayout.nextChildVerticalRect(panel);
			
			//create and add the element
			final var fill = new TFillColorElement(n1.x, n1.y + GAP, n1.width, GeneralStatWidget.HEIGHT);
			fill.setColor(TPanelElement.COLOR_BACKGROUND);
			panel.addChild(fill, false);
			
			//create and add the element's label
			final var lbl_noStats = new TLabelElement(0, 0, fill.getWidth(), fill.getHeight());
			lbl_noStats.setText(TXT_NO_STATS_YET);
			lbl_noStats.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
			fill.addChild(lbl_noStats, true);
		}
	}
	// --------------------------------------------------
	protected final @Override void initExtraFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initGroupByFilter(initContext);
		StatsTabUtils.initSortCustomsByFilter(initContext);
		StatsTabUtils.initTimeFormatFilter(initContext);
		StatsTabUtils.initDistanceFormatFilter(initContext);
	}
	// ==================================================
	/**
	 * Returns the {@link FilterGroupBy} that'll be used by "default".
	 * @apiNote Must not return {@code null}.
	 */
	protected @Virtual FilterGroupBy getDefaultGroupFilter() { return FilterGroupBy.DEFAULT; }
	// --------------------------------------------------
	/**
	 * Initializes a {@link Collection} of {@link SUGeneralStat}s onto a {@link TPanelElement}.
	 * @param panel The {@link TPanelElement}.
	 * @param stats The {@link SUGeneralStat}s to initialize.
	 * @param processWidget Optional {@link Consumer} that allows you to make changes to widgets as they are created.
	 */
	protected final void initStats(
			TPanelElement panel,
			Collection<SUGeneralStat> stats,
			Consumer<GeneralStatWidget> processWidget)
	{
		//iterate all general stats that are to be added
		for(final SUGeneralStat stat : stats)
		{
			//calculate the next XYWH
			final var n1 = UILayout.nextChildVerticalRect(panel);
			
			//create and add the stat widget
			final var statWidget = new GeneralStatWidget(n1.x, n1.y + GAP, n1.width, stat);
			panel.addChild(statWidget, false);
			
			//process the stat widget
			if(processWidget != null)
				processWidget.accept(statWidget);
		}
	}
	//
	/**
	 * Default {@link GeneralStatWidget} processing logic.<br/>
	 * Primarily used for {@link #initStats(TPanelElement, Collection, Consumer)}
	 * @param widget The {@link GeneralStatWidget}.
	 */
	protected @Virtual void processWidget(GeneralStatWidget widget)
	{
		widget.eContextMenu.register((__, cMenu) ->
		{
			//do not add the "pin to hud" button if viewing third-party stats
			//(because pinning 3rd party stats is not supported yet...)
			if(!(widget.getStat().getStatsProvider() instanceof LocalPlayerStatsProvider))
				return;
			
			//continue as usual otherwise...
			cMenu.addButton(BST.hud_pinStat(), ___ ->
			{
				final var hud = BetterStatsHudScreen.getInstance();
				hud.setParentScreen(MC_CLIENT.screen);
				hud.addEntry(new StatsHudGeneralEntry(widget.getStat(), widget.getFormatter()));
				MC_CLIENT.setScreen(hud.getAsScreen());
			});
			cMenu.addButton(translatable("mco.selectServer.close"), ___ -> {});
		});
	}
	// --------------------------------------------------
	/**
	 * Initializes "debug mode" info.
	 * @param panel The {@link TPanelElement} to initialize to.
	 */
	protected static final void initDebugInfo(TPanelElement panel)
	{
		//init the group label
		StatsTabUtils.initGroupLabel(panel, translatable("createWorld.tab.world.title"))
			.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//init the "world stat panel"
		final var n1 = UILayout.nextChildVerticalRect(panel);
		final var wsp = new TFillColorElement(n1.x, n1.y, n1.width, (CustomStatElement.HEIGHT * 2) + GAP);
		wsp.setColor(TPanelElement.COLOR_BACKGROUND);
		panel.addChild(wsp, false);
		
		//init the world name and seed gui stats
		final var world = MC_CLIENT.level;
		final var ws1 = new CustomStatElement(
			wsp.getX(), wsp.getY(), wsp.getWidth(),
			translatable("selectWorld.enterName"),
			literal(Objects.toString((world != null) ? world.dimension().location() : "-"))
		);
		final var ws2 = new CustomStatElement(
			wsp.getX(), wsp.getEndY() - CustomStatElement.HEIGHT, wsp.getWidth(),
			BST.sTab_hashedSeed(),
			literal(Objects.toString(getBiomeAccessSeed(world.getBiomeManager())))
		);
		wsp.addChild(ws1, false);
		wsp.addChild(ws2, false);
	}
	// ==================================================
}