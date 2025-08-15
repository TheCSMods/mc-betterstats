package io.github.thecsdev.betterstats.api.client.gui.util;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel.BS_WIDGETS_TEXTURE;
import static io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout.nextChildBottomY;
import static io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout.nextChildVerticalRect;
import static io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext.DEFAULT_TEXT_COLOR;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Rectangle;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.widget.SelectStatsTabWidget;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.FiltersInitContext;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.registry.BSRegistries;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortCustomsBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortItemsBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortMobsBy;
import io.github.thecsdev.betterstats.api.util.formatters.StatValueFormatter;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTextureElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TCheckboxWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TSelectEnumWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TSelectWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TTextFieldWidget;
import io.github.thecsdev.tcdcommons.api.util.collections.GenericProperties;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;

/**
 * Some utility methods for initializing and handling {@link StatsTab} GUIs.
 */
public final class StatsTabUtils
{
	// ==================================================
	private StatsTabUtils() {}
	// --------------------------------------------------
	/**
	 * The default horizontal and vertical margin {@link BetterStats}
	 * applies to various GUI elements on the {@link BetterStatsScreen}.
	 */
	public static final int GAP = 3;
	// --------------------------------------------------
	/**
	 * The {@link ResourceLocation} of the "search query" filter.
	 * <p>
	 * <b>Filter type:</b> {@link String}
	 * @see StatFilterSettings
	 * @see GenericProperties#getPropertyOrDefault(Object, Object)
	 */
	public static final ResourceLocation FILTER_ID_SEARCH = ResourceLocation.fromNamespaceAndPath(getModID(), "search_query");
	
	/**
	 * The {@link ResourceLocation} of the "show empty stats" filter.
	 * <p>
	 * <b>Filter type:</b> {@link Boolean}
	 * @see StatFilterSettings
	 * @see GenericProperties#getPropertyOrDefault(Object, Object)
	 */
	public static final ResourceLocation FILTER_ID_SHOWEMPTY = ResourceLocation.fromNamespaceAndPath(getModID(), "show_empty_stats");
	
	/**
	 * The {@link ResourceLocation} of the "group by" filter.
	 * <p>
	 * <b>Filter type:</b> {@link Enum}&lt;{@link FilterGroupBy}&gt;
	 * @see StatFilterSettings
	 * @see GenericProperties#getPropertyOrDefault(Object, Object)
	 */
	public static final ResourceLocation FILTER_ID_GROUP = ResourceLocation.fromNamespaceAndPath(getModID(), "group_by");
	
	/**
	 * The {@link ResourceLocation} of the "sort by" filter for {@link SUGeneralStat} statistics.
	 * <p>
	 * <b>Filter type:</b> {@link Enum}&lt;{@link FilterSortCustomsBy}&gt;
	 * @see StatFilterSettings
	 * @see GenericProperties#getPropertyOrDefault(Object, Object)
	 */
	public static final ResourceLocation FILTER_ID_SORT_CUSTOMS = ResourceLocation.fromNamespaceAndPath(getModID(), "sort_customs_by");
	
	/**
	 * The {@link ResourceLocation} of the "sort by" filter for {@link SUItemStat} statistics.
	 * <p>
	 * <b>Filter type:</b> {@link Enum}&lt;{@link FilterSortItemsBy}&gt;
	 * @see StatFilterSettings
	 * @see GenericProperties#getPropertyOrDefault(Object, Object)
	 */
	public static final ResourceLocation FILTER_ID_SORT_ITEMS = ResourceLocation.fromNamespaceAndPath(getModID(), "sort_items_by");
	
	/**
	 * The {@link ResourceLocation} of the "sort by" filter for {@link SUMobStat} statistics.
	 * <p>
	 * <b>Filter type:</b> {@link Enum}&lt;{@link FilterSortMobsBy}&gt;
	 * @see StatFilterSettings
	 * @see GenericProperties#getPropertyOrDefault(Object, Object)
	 */
	public static final ResourceLocation FILTER_ID_SORT_MOBS = ResourceLocation.fromNamespaceAndPath(getModID(), "sort_mobs_by");
	
	/**
	 * The {@link ResourceLocation} of the filter for time-based general-stat value formatting.
	 * <p>
	 * <b>Filter type:</b> {@link StatValueFormatter}
	 * @since 3.13
	 */
	public static final ResourceLocation FILTER_ID_FORMAT_TIME = ResourceLocation.fromNamespaceAndPath(getModID(), "sfv_time");
	
	/**
	 * The {@link ResourceLocation} of the filter for time-based general-stat value formatting.
	 * <p>
	 * <b>Filter type:</b> {@link StatValueFormatter}
	 * @since 3.13
	 */
	public static final ResourceLocation FILTER_ID_FORMAT_DISTANCE = ResourceLocation.fromNamespaceAndPath(getModID(), "sfv_distance");
	// ==================================================
	/**
	 * Initializes the GUI for the most basic filters such as the
	 * {@link SelectStatsTabWidget} and the search bar.
	 * @param initContext The {@link FiltersInitContext}.
	 */
	public static void initDefaultFilters(FiltersInitContext initContext)
	{
		//obtain important stuff
		final var filterSettings = initContext.getFilterSettings();
		final var panel = initContext.getFiltersPanel();
		
		//init the filters label
		final var lbl_filters = initGroupLabel(panel, StatFiltersPanel.TXT_FILTERS);
		lbl_filters.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		lbl_filters.setTextColor(DEFAULT_TEXT_COLOR);
		
		//init the stats tab select widget
		final var statsTab = initContext.getSelectedStatsTab();
		final var n1 = nextChildVerticalRect(panel);
		final var select_tab = new SelectStatsTabWidget(n1.x, n1.y + panel.getScrollPadding(), n1.width, n1.height);
		try { select_tab.setSelected(statsTab); } catch(NoSuchElementException nse) { select_tab.setSelected((StatsTab)null); }
		select_tab.setText(statsTab != null ? statsTab.getName() : literal("-"));
		select_tab.eSelectionChanged.register((__, sel) -> initContext.setSelectedStatsTab(sel.getStatsTab()));
		panel.addChild(select_tab, false);
		
		//init the search bar
		final var n2 = nextChildVerticalRect(panel);
		final var input_search = new TTextFieldWidget(n2.x, n2.y + GAP, n2.width, n2.height);
		input_search.setInput(filterSettings.getPropertyOrDefault(FILTER_ID_SEARCH, ""));
		input_search.setPlaceholderText(translatable("gui.socialInteractions.search_hint"));
		input_search.eTextChanged.register((__, txt) ->
		{
			filterSettings.setProperty(FILTER_ID_SEARCH, (String)txt);
			initContext.refreshStatsTab();
		});
		panel.addChild(input_search, false);
	}
	// --------------------------------------------------
	/**
	 * Initializes the GUI {@link TCheckboxWidget} for the "show empty stats" filter.
	 * @param initContext The {@link FiltersInitContext}.
	 */
	public static void initShowEmptyStatsFilter(FiltersInitContext initContext)
	{
		//obtain important stuff
		final var filterSettings = initContext.getFilterSettings();
		final var panel = initContext.getFiltersPanel();
		final int gap = GAP * (panel.getChildren().size() == 0 ? 0 : 1);
		
		//init the "show empty stats" checkbox
		final var n1 = nextChildVerticalRect(panel);
		final var check_showEmpty = new TCheckboxWidget(n1.x, n1.y + gap, n1.width, n1.height);
		check_showEmpty.setText(BST.filter_showEmptyStats());
		check_showEmpty.setChecked(filterSettings.getPropertyOrDefault(FILTER_ID_SHOWEMPTY, false));
		check_showEmpty.eClicked.register(__ ->
		{
			filterSettings.setProperty(FILTER_ID_SHOWEMPTY, check_showEmpty.getChecked());
			initContext.refreshStatsTab();
		});
		panel.addChild(check_showEmpty, false);
	}
	// --------------------------------------------------
	/**
	 * Initializes the GUI {@link TSelectEnumWidget} for the "group by" filter.
	 * @param initContext The {@link FiltersInitContext}.
	 */
	public static void initGroupByFilter(FiltersInitContext initContext)
	{
		final var tex = new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 0, 20, 20));
		initEnumFilter(initContext, tex, FILTER_ID_GROUP, FilterGroupBy.DEFAULT);
	}
	
	/**
	 * Initializes the GUI {@link TSelectEnumWidget} for the "sort by" filter for {@link SUGeneralStat}s.
	 * @param initContext The {@link FiltersInitContext}.
	 */
	public static void initSortCustomsByFilter(FiltersInitContext initContext)
	{
		final var tex = new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 20, 20, 20));
		initEnumFilter(initContext, tex, FILTER_ID_SORT_CUSTOMS, FilterSortCustomsBy.DEFAULT);
	}
	
	/**
	 * Initializes the GUI {@link TSelectEnumWidget} for the "sort by" filter for {@link SUItemStat}s.
	 * @param initContext The {@link FiltersInitContext}.
	 */
	public static void initSortItemsByFilter(FiltersInitContext initContext)
	{
		final var tex = new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 20, 20, 20));
		initEnumFilter(initContext, tex, FILTER_ID_SORT_ITEMS, FilterSortItemsBy.DEFAULT);
	}
	
	/**
	 * Initializes the GUI {@link TSelectEnumWidget} for the "sort by" filter for {@link SUMobStat}s.
	 * @param initContext The {@link FiltersInitContext}.
	 */
	public static void initSortMobsByFilter(FiltersInitContext initContext)
	{
		final var tex = new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 20, 20, 20));
		initEnumFilter(initContext, tex, FILTER_ID_SORT_MOBS, FilterSortMobsBy.DEFAULT);
	}
	// --------------------------------------------------
	/**
	 * Initializes the GUI for an {@link Enum}-based filter.
	 * @param <E> The {@link Enum} type.
	 * @param initContext The {@link FiltersInitContext}.
	 * @param icon An optional icon that will be shown beside the {@link TSelectEnumWidget}.
	 * @param filterId The filter's unique {@link ResourceLocation} used for getting and setting the filter's value.
	 * @param defaultValue If the filter value is undefined, this will be used as the default one. Must not be {@code null}.
	 * @throws NullPointerException If an argument is {@code null}, except for the {@link UITexture}.
	 */
	public static <E extends Enum<E>> void initEnumFilter(
			final FiltersInitContext initContext,
			final @Nullable UITexture icon,
			final ResourceLocation filterId,
			final E defaultValue) throws NullPointerException
	{
		//requirements
		Objects.requireNonNull(initContext);
		Objects.requireNonNull(filterId);
		Objects.requireNonNull(defaultValue);
		
		//obtain important stuff
		final var filterSettings = initContext.getFilterSettings();
		final var panel = initContext.getFiltersPanel();
		final int gap = GAP * (panel.getChildren().size() == 0 ? 0 : 1);
		
		//obtain enum filter value
		final E filterValue = filterSettings.getPropertyOrDefault(filterId, defaultValue);
		
		//obtain the next placement coordinates
		final var n1 = nextChildVerticalRect(panel);
		n1.y += gap;
		if(icon != null)
		{
			final int i = n1.height + GAP;
			n1.x += i;
			n1.width -= i;
			
			//init the icon
			final var ico = new TTextureElement(n1.x - i, n1.y, n1.height, n1.height, icon);
			panel.addChild(ico, false);
		}
		
		//init the select widget
		final TSelectEnumWidget<E> sel = new TSelectEnumWidget<E>(n1.x, n1.y, n1.width, n1.height, filterValue);
		sel.eSelectionChanged.register((__, newValue) ->
		{
			filterSettings.setProperty(filterId, newValue.getEnumValue());
			initContext.refreshStatsTab();
		});
		panel.addChild(sel, false);
	}
	// ==================================================
	/**
	 * Initializes a {@link TSelectWidget} filter for time-based {@link StatValueFormatter}s.
	 * @param initContext The {@link FiltersInitContext}.
	 * @since 3.13
	 */
	public static void initTimeFormatFilter(FiltersInitContext initContext)
	{
		initStatValueFormatFilter(
				initContext,
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(40, 0, 20, 20)),
				FILTER_ID_FORMAT_TIME,
				StreamSupport.stream(BSRegistries.STAT_TIME_FORMATTER.spliterator(), false)
					.map(Map.Entry::getValue)
					.collect(Collectors.toList()),
				StatValueFormatter.TIME);
	}
	
	/**
	 * Initializes a {@link TSelectWidget} filter for distance-based {@link StatValueFormatter}s.
	 * @param initContext The {@link FiltersInitContext}.
	 * @since 3.13
	 */
	public static void initDistanceFormatFilter(FiltersInitContext initContext)
	{
		initStatValueFormatFilter(
				initContext,
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(40, 20, 20, 20)),
				FILTER_ID_FORMAT_DISTANCE,
				StreamSupport.stream(BSRegistries.STAT_DISTANCE_FORMATTER.spliterator(), false)
					.map(Map.Entry::getValue)
					.collect(Collectors.toList()),
				StatValueFormatter.DISTANCE);
	}
	// --------------------------------------------------
	/**
	 * Initializes a {@link TSelectWidget} filter for a collection of {@link StatValueFormatter}s.
	 * @param initContext The {@link FiltersInitContext}.
	 * @param icon An optional icon that will be shown beside the {@link TSelectWidget}.
	 * @param filterId The filter's unique {@link ResourceLocation} used for getting and setting the filter's value.
	 * @param formats The collection of {@link StatValueFormatter}s to feature in the {@link TSelectWidget}.
	 * @param defaultValue The default filter value.
	 * @since 3.13
	 */
	public static void initStatValueFormatFilter(FiltersInitContext initContext,
			final @Nullable UITexture icon,
			final ResourceLocation filterId,
			Iterable<StatValueFormatter> formats,
			StatValueFormatter defaultValue)
	{
		//requirements
		Objects.requireNonNull(initContext);
		Objects.requireNonNull(filterId);
		Objects.requireNonNull(formats);
		Objects.requireNonNull(defaultValue);
		
		//obtain important stuff
		final var filterSettings = initContext.getFilterSettings();
		final var panel          = initContext.getFiltersPanel();
		final int gap            = GAP * (panel.getChildren().size() == 0 ? 0 : 1);

		//obtain the next placement coordinates
		final var n1 = nextChildVerticalRect(panel);
		n1.y += gap;
		if(icon != null)
		{
			final int i = n1.height + GAP;
			n1.x += i;
			n1.width -= i;
			
			//init the icon
			final var ico = new TTextureElement(n1.x - i, n1.y, n1.height, n1.height, icon);
			panel.addChild(ico, false);
		}
		
		//init the select widget
		final var sel = new TSelectWidget<TSelectWidget.SimpleEntry>(n1.x, n1.y, n1.width, n1.height);
		final var val = filterSettings.getPropertyOrDefault(filterId, defaultValue);
		formats.forEach(format ->
		{
			final var entry = new TSelectWidget.SimpleEntry(format.getDisplayName(), () ->
			{
				filterSettings.setProperty(filterId, format);
				initContext.refreshStatsTab();
			});
			sel.addEntry(entry);
		});
		sel.setText(val.getDisplayName());
		panel.addChild(sel, false);
	}
	// ==================================================
	/**
	 * Initializes a {@link TLabelElement} that represents a group of
	 * elements that come after it. For example, this is used to to
	 * display the "group labels" for mob and item stats.
	 * @param panel The target {@link TPanelElement}.
	 * @param text The {@link TLabelElement}'s {@link Component}.
	 */
	public static TLabelElement initGroupLabel(TPanelElement panel, Component text)
	{
		final int nextX = panel.getScrollPadding();
		final int nextY = (nextChildBottomY(panel) - panel.getY()) + (panel.getChildren().size() != 0 ? 10 : 0);
		final int nextW = panel.getWidth() - (nextX * 2);
		
		final var label = new TLabelElement(nextX, nextY, nextW, GeneralStatWidget.HEIGHT, text);
		label.setTextColor(BSStatsTabs.COLOR_SPECIAL);
		label.setTextSideOffset(0);
		panel.addChild(label, true);
		return label;
	}
	// ==================================================
}
