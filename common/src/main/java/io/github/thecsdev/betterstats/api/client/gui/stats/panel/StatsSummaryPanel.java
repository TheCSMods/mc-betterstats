package io.github.thecsdev.betterstats.api.client.gui.stats.panel;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.StatType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.registry.BSRegistries;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;

/**
 * A {@link BSComponentPanel} that summarizes given {@link Collection}s
 * of {@link SUItemStat}s and {@link SUMobStat}s.
 */
public final class StatsSummaryPanel extends BSComponentPanel
{
	// ==================================================
	public static final int ENTRY_HEIGHT = GeneralStatWidget.HEIGHT;
	// --------------------------------------------------
	/**
	 * {@link StatsSummaryPanel} entries.
	 * @apiNote Each entry <b>must</b> be a {@link Component} {@code array} with the length of 3.
	 */
	private final List<Component[]> entries = new ArrayList<>();
	// --------------------------------------------------
	protected int columnCount = 2;
	// ==================================================
	public StatsSummaryPanel(int x, int y, int width) { this(x, y, width, 10 + (ENTRY_HEIGHT * 3)); }
	public StatsSummaryPanel(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		this.scrollFlags = TPanelElement.SCROLL_VERTICAL;
		this.scrollPadding = 5;
	}
	// ==================================================
	/**
	 * Sets the number of columns that will be used to summarize stats.
	 * @apiNote Argument is clamped to the range from 1 to 4.
	 */
	public final void setColumnCount(int columnCount)
	{
		this.columnCount = Mth.clamp(columnCount, 1, 5);
		if(getParentTScreen() != null) refresh();
	}
	// --------------------------------------------------
	public final void clearEntries() { this.entries.clear(); }
	public final void addEntry(@Nullable Component left, @Nullable Component right) { addEntry(left, null, right); }
	public final void addEntry(@Nullable Component left, @Nullable Component center, @Nullable Component right) { this.entries.add(new Component[] { left, center, right }); }
	// --------------------------------------------------
	/**
	 * Adds {@link Component}ual entries to this {@link StatsSummaryPanel} that
	 * summarize a collection of {@link SUItemStat}'s stats.
	 */
	public final void summarizeItemStats(Iterable<SUItemStat> itemStats)
	{
		//first clear any existing entries
		clearEntries();
		
		//create a map to track all the totals
		final var map = new LinkedHashMap<StatType<?>, Long>();
		
		//initialize the map, and count the stats totals
		for(final var statType : BuiltInRegistries.STAT_TYPE)
		{
			//check stat type registry
			final var stR = statType.getRegistry();
			final var isBlock = (stR == BuiltInRegistries.BLOCK);
			final var isItem = (stR == BuiltInRegistries.ITEM);
			if(!(isBlock || isItem)) continue;
			final @SuppressWarnings("unchecked") var statTypeO = (StatType<Object>)statType;
			
			//start counting
			long count = 0;
			for(final var itemStat : itemStats)
				count += itemStat.getStatsProvider().getStatValue(
						statTypeO,
						isItem ? itemStat.getItem() : itemStat.getBlock());
			
			//add the count to the map
			map.put(statType, count);
		}
		
		//add entries
		map.forEach((statType, statValue) -> addEntry(statType.getDisplayName(), literal(Long.toString(statValue))));
		
		//refresh if needed
		if(getParentTScreen() != null) refresh();
	}
	
	/**
	 * Adds {@link Component}ual entries to this {@link StatsSummaryPanel} that
	 * summarize a collection of {@link SUMobStat}'s stats.
	 */
	public final void summarizeMobStats(Iterable<SUMobStat> mobStats)
	{
		//first clear any existing entries
		clearEntries();
		
		//create a map to track all the totals
		final var map = new LinkedHashMap<StatType<EntityType<?>>, Long>();
		
		//initialize the map, and count the stats totals
		for(final var statType : BuiltInRegistries.STAT_TYPE)
		{
			//check stat type registry
			if(statType.getRegistry() != BuiltInRegistries.ENTITY_TYPE) continue;
			final @SuppressWarnings("unchecked") var statTypeE = (StatType<EntityType<?>>)statType;
			
			//start counting
			long count = 0;
			for(final var mobStat : mobStats)
				count += mobStat.getStatsProvider().getStatValue(statTypeE, mobStat.getEntityType());
			
			//add the count to the map
			map.put(statTypeE, count);
		}
		
		//add entries
		map.forEach((statType, statValue) ->
		{
			final var phrase = BSRegistries.getEntityStatTypePhrase(statType);
			addEntry(phrase, literal(Long.toString(statValue)));
		});
		
		//refresh if needed
		if(getParentTScreen() != null) refresh();
	}
	// ==================================================
	protected final @Override void init()
	{
		//calculate entry width and height
		final int sp = getScrollPadding();
		final int entryWidth = (getWidth() - (sp * 2)) / Math.max(this.columnCount, 1);
		
		//define the current "row/column" "cursor" position, starting from 0
		int row = 0, column = 0;
		
		//iterate all entries
		for(final Component[] entry : this.entries)
		{
			//calculate entry X and Y
			final int eX = sp + (column * entryWidth);
			final int eY = sp + (row * ENTRY_HEIGHT);
			
			//create entry labels
			int ha = 0; //keeps track of horizontal alignment
			for(final Component entryText : entry)
			{
				//keep track of horizontal alignment, and skip null texts
				ha++;
				if(entryText == null) continue;
				
				//create the label and assign its corresponding horizontal alignment
				final var lbl = new TLabelElement(eX, eY, entryWidth, ENTRY_HEIGHT, entryText);
				lbl.setTextSideOffset(TDrawContext.DEFAULT_TEXT_SIDE_OFFSET);
				switch(ha)
				{
					case 1: lbl.setTextHorizontalAlignment(HorizontalAlignment.LEFT); break;
					case 2: lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER); break;
					case 3: lbl.setTextHorizontalAlignment(HorizontalAlignment.RIGHT); break;
					default: break;
				}
				
				//add the label
				addChild(lbl, true);
			}
			
			//increment row and column
			column++;
			if(column >= this.columnCount) { column = 0; row++; }
		}
	}
	// --------------------------------------------------
	/**
	 * Resizes this {@link StatsSummaryPanel} to fit the children labels on the Y axis.
	 */
	public final void autoHeight()
	{
		//old implementation that only works when initialized
		/*if(getChildren().size() < 1) return;
		final int startY = getY();
		final int endY = getChildren().getLastChild().getEndY();
		final int height = (endY - startY) + getScrollPadding();
		setSize(getWidth(), height);*/
		
		//new implementation that works even when not initialized
		final int rows = (int) Math.ceil(((double)this.entries.size() / Math.max(this.columnCount, 1)));
		final int height = (rows * ENTRY_HEIGHT) + (getScrollPadding() * 2);
		setSize(getWidth(), height);
	}
	// ==================================================
}