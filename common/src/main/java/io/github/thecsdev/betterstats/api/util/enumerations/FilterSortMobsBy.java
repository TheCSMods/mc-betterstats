package io.github.thecsdev.betterstats.api.util.enumerations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;

/**
 * A statistics filter {@link Enum} that dictates how
 * {@link SUMobStat} statistics entries are sorted.
 */
public enum FilterSortMobsBy implements ITextProvider
{
	// ==================================================
	DEFAULT(BST.filter_groupBy_default()),
	KILLS(MobStatType.KILLED.getText()),
	DEATHS(MobStatType.KILLED_BY.getText());
	// ==================================================
	private final Component text;
	// --------------------------------------------------
	private FilterSortMobsBy(Component text) { this.text = Objects.requireNonNull(text); }
	public final @Override Component getText() { return this.text; }
	// ==================================================
	/**
	 * Sorts a {@link Map} of {@link SUMobStat}s based on {@code this} {@link FilterSortMobsBy}.
	 * @param stats The {@link SUMobStat}s to sort.
	 */
	public final void sortMobStats(Map<?, List<SUMobStat>> stats) { sortMobStats(stats, this); }
	
	/**
	 * Sorts a {@link List}&lt;{@link SUMobStat}&gt; based on {@code this} {@link FilterSortMobsBy}.
	 * @param stats The {@link SUMobStat}s to sort.
	 */
	public final void sortMobStats(List<SUMobStat> stats) { sortMobStats(stats, this); }
	// --------------------------------------------------
	/**
	 * Sorts a {@link Map} of {@link SUMobStat}s based on {@link FilterSortMobsBy}.
	 * @param stats The {@link SUMobStat}s to sort.
	 * @param sortBy The {@link FilterSortMobsBy}.
	 */
	public static final void sortMobStats(Map<?, List<SUMobStat>> stats, FilterSortMobsBy sortBy)
	{
		for(final var entry : stats.entrySet())
			sortMobStats(entry.getValue(), sortBy);
	}
	
	/**
	 * Sorts a {@link List}&lt;{@link SUMobStat}&gt; based on {@link FilterSortMobsBy}.
	 * @param stats The {@link SUMobStat}s to sort.
	 * @param sortBy The {@link FilterSortMobsBy}.
	 */
	public static final void sortMobStats(List<SUMobStat> stats, FilterSortMobsBy sortBy)
	{
		switch(sortBy)
		{
			case KILLS:  Collections.sort(stats, (s1, s2) -> Integer.compare(s2.kills, s1.kills)); break;
			case DEATHS: Collections.sort(stats, (s1, s2) -> Integer.compare(s2.deaths, s1.deaths)); break;
			default: break;
		}
	}
	// ==================================================
}