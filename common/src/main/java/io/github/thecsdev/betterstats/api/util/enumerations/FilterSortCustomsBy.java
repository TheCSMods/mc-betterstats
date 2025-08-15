package io.github.thecsdev.betterstats.api.util.enumerations;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.betterstats.api.util.stats.SUPlayerBadgeStat;
import io.github.thecsdev.betterstats.api.util.stats.SUStat;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;

/**
 * A statistics filter {@link Enum} that dictates how
 * {@link SUGeneralStat} statistics entries are sorted.
 */
public enum FilterSortCustomsBy implements ITextProvider
{
	// ==================================================
	/**
	 * Sorts statistics entries in the "default" order.<br/>
	 * This usually ends up displaying entries in the order their items
	 * are registered in their corresponding registries.
	 */
	DEFAULT(BST.filter_groupBy_default()),
	
	/**
	 * A-Z<br/>
	 * Sorts statistics entries in the alphabetical order, based on entry names.
	 */
    ALPHABETICAL(literal("A-Z")),
    
    /**
	 * Z-A<br/>
	 * Sorts statistics entries in the reverse-alphabetical order, based on entry names.
	 * 
	 * @apiNote Couldn't come up with a better name, so I literally reversed the word.
	 */
    LACITEBAHPLA(literal("Z-A")),
    
    /**
     * 0-9<br/>
     * Sorts statistics entries based on the entry {@link Integer} values.
     */
    INCREMENTAL(literal("0-9")),
    
    /**
     * 9-0<br/>
     * Sorts statistics entries based on the entry {@link Integer} values.
     */
    DECREMENTAL(literal("9-0"));
	// ==================================================
	private final Component text;
	// --------------------------------------------------
	private FilterSortCustomsBy(Component text) { this.text = Objects.requireNonNull(text); }
	public final @Override Component getText() { return this.text; }
	// ==================================================
	/**
	 * Sorts a {@link List}&lt;{@link SUStat}&gt; based on {@link FilterSortCustomsBy}.
	 * @param stats The {@link List}&lt;{@link SUStat}&gt; to sort.
	 * @param valueSupplier The {@link Function} that supplies the {@link Integer} value of a given {@link SUStat}.
	 */
	public final <E extends SUStat<?>> void sortStats(List<E> stats, Function<E, Integer> valueSupplier)
	{
		sortStats(stats, valueSupplier, this);
	}
	
	/**
	 * Sorts a {@link List}&lt;{@link SUGeneralStat}&gt; based on {@code this} {@link FilterSortCustomsBy}.
	 * @param stats The {@link List}&lt;{@link SUGeneralStat}&gt; to sort.
	 */
	public final void sortGeneralStats(List<SUGeneralStat> stats) { sortGeneralStats(stats, this); }
	
	/**
	 * Sorts a {@link Map} of {@link SUPlayerBadgeStat}s based on {@code this} {@link FilterSortCustomsBy}.
	 * @param stats The {@link Map} of {@link SUPlayerBadgeStat}s to sort.
	 */
	public final void sortPlayerBadgeStats(Map<?, List<SUPlayerBadgeStat>> stats) { sortPlayerBadgeStats(stats, this); }
	
	/**
	 * Sorts a {@link List}&lt;{@link SUPlayerBadgeStat}&gt; based on {@code this} {@link FilterSortCustomsBy}.
	 * @param stats The {@link List}&lt;{@link SUPlayerBadgeStat}&gt; to sort.
	 */
	public final void sortPlayerBadgeStats(List<SUPlayerBadgeStat> stats) { sortPlayerBadgeStats(stats, this); }
	// --------------------------------------------------
	/**
	 * Sorts a {@link List}&lt;{@link SUStat}&gt; based on {@link FilterSortCustomsBy}.
	 * @param stats The {@link List}&lt;{@link SUStat}&gt; to sort.
	 * @param valueSupplier The {@link Function} that supplies the {@link Integer} value of a given {@link SUStat}.
	 * @param sortBy The {@link FilterSortCustomsBy}.
	 */
	public static final <E extends SUStat<?>> void sortStats(
			List<E> stats,
			Function<E, Integer> valueSupplier,
			FilterSortCustomsBy sortBy)
	{
		switch(sortBy)
		{
			case ALPHABETICAL: Collections.sort(stats, Comparator.comparing(stat -> stat.getStatLabel().getString())); break;
			case LACITEBAHPLA: Collections.sort(stats, Comparator.comparing((SUStat<?> stat) -> stat.getStatLabel().getString()).reversed()); break;
			case INCREMENTAL: Collections.sort(stats, (s1, s2) -> Integer.compare(valueSupplier.apply(s1), valueSupplier.apply(s2))); break;
			case DECREMENTAL: Collections.sort(stats, (s1, s2) -> Integer.compare(valueSupplier.apply(s2), valueSupplier.apply(s1))); break;
			default: break;
		}
	}
	
	/**
	 * Sorts a {@link List}&lt;{@link SUGeneralStat}&gt; based on {@link FilterSortCustomsBy}.
	 * @param stats The {@link List}&lt;{@link SUGeneralStat}&gt; to sort.
	 * @param sortBy The {@link FilterSortCustomsBy}.
	 */
	public static final void sortGeneralStats(List<SUGeneralStat> stats, FilterSortCustomsBy sortBy)
	{
		sortStats(stats, stat -> stat.value, sortBy);
	}
	
	/**
	 * Sorts a {@link Map} of {@link SUPlayerBadgeStat}s based on {@link FilterSortCustomsBy}.
	 * @param stats The {@link Map} of {@link SUPlayerBadgeStat}s to sort.
	 * @param sortBy The {@link FilterSortCustomsBy}.
	 */
	public static final void sortPlayerBadgeStats(Map<?, List<SUPlayerBadgeStat>> stats, FilterSortCustomsBy sortBy)
	{
		for(final var entry : stats.entrySet())
			sortPlayerBadgeStats(entry.getValue(), sortBy);
	}
	
	/**
	 * Sorts a {@link List}&lt;{@link SUPlayerBadgeStat}&gt; based on {@link FilterSortCustomsBy}.
	 * @param stats The {@link List}&lt;{@link SUPlayerBadgeStat}&gt; to sort.
	 * @param sortBy The {@link FilterSortCustomsBy}.
	 */
	public static final void sortPlayerBadgeStats(List<SUPlayerBadgeStat> stats, FilterSortCustomsBy sortBy)
	{
		sortStats(stats, stat -> stat.value, sortBy);
	}
	// ==================================================
}