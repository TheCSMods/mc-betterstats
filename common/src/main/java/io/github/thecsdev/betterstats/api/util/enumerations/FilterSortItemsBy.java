package io.github.thecsdev.betterstats.api.util.enumerations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;

/**
 * A statistics filter {@link Enum} that dictates how
 * {@link SUItemStat} statistics entries are sorted.
 */
public enum FilterSortItemsBy implements ITextProvider
{
	// ==================================================
	DEFAULT(BST.filter_groupBy_default()),
    MINED(ItemStatType.MINED.getText()),
    CRAFTED(ItemStatType.CRAFTED.getText()),
    USED(ItemStatType.USED.getText()),
    BROKEN(ItemStatType.BROKEN.getText()),
    PICKED_UP(ItemStatType.PICKED_UP.getText()),
    DROPPED(ItemStatType.DROPPED.getText());
	// ==================================================
	private final Component text;
	// --------------------------------------------------
	private FilterSortItemsBy(Component text) { this.text = Objects.requireNonNull(text); }
	public final @Override Component getText() { return this.text; }
	// ==================================================
	/**
	 * Sorts a {@link Map} of {@link SUItemStat}s based on {@code this} {@link FilterSortItemsBy}.
	 * @param stats The {@link SUItemStat}s to sort.
	 */
	public final void sortItemStats(Map<?, List<SUItemStat>> stats) { sortItemStats(stats, this); }
	
	/**
	 * Sorts a {@link List}&lt;{@link SUItemStat}&gt; based on {@code this} {@link FilterSortItemsBy}.
	 * @param stats The {@link SUItemStat}s to sort.
	 */
	public final void sortItemStats(List<SUItemStat> stats) { sortItemStats(stats, this); }
	// --------------------------------------------------
	/**
	 * Sorts a {@link Map} of {@link SUItemStat}s based on {@link FilterSortItemsBy}.
	 * @param stats The {@link SUItemStat}s to sort.
	 * @param sortBy The {@link FilterSortItemsBy}.
	 */
	public static final void sortItemStats(Map<?, List<SUItemStat>> stats, FilterSortItemsBy sortBy)
	{
		for(final var entry : stats.entrySet())
			sortItemStats(entry.getValue(), sortBy);
	}
	
	/**
	 * Sorts a {@link List}&lt;{@link SUItemStat}&gt; based on {@link FilterSortItemsBy}.
	 * @param stats The {@link SUItemStat}s to sort.
	 * @param sortBy The {@link FilterSortItemsBy}.
	 */
	public static final void sortItemStats(List<SUItemStat> stats, FilterSortItemsBy sortBy)
	{
		switch(sortBy)
		{
			case MINED:     Collections.sort(stats, (s1, s2) -> Integer.compare(s2.mined, s1.mined)); break;
			case CRAFTED:   Collections.sort(stats, (s1, s2) -> Integer.compare(s2.crafted, s1.crafted)); break;
			case USED:      Collections.sort(stats, (s1, s2) -> Integer.compare(s2.used, s1.used)); break;
			case BROKEN:    Collections.sort(stats, (s1, s2) -> Integer.compare(s2.broken, s1.broken)); break;
			case PICKED_UP: Collections.sort(stats, (s1, s2) -> Integer.compare(s2.pickedUp, s1.pickedUp)); break;
			case DROPPED:   Collections.sort(stats, (s1, s2) -> Integer.compare(s2.dropped, s1.dropped)); break;
			default: break;
		}
	}
	// ==================================================
}