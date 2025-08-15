package io.github.thecsdev.betterstats.api.util.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fTranslatable;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.util.BST;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class SUGeneralStat extends SUStat<ResourceLocation>
{
	// ==================================================
	public static final Component TEXT_VALUE = BST.sWidget_general_value();
	// --------------------------------------------------
	private final Stat<ResourceLocation> stat;
	private final boolean isEmpty; //cached value to avoid re-calculations
	
	/**
	 * The raw {@link Integer} value of this {@link Stat}.
	 */
	public final int value;
	
	/**
	 * The formatted {@link Component}ual user-friendly version of this {@link Stat}'s {@link #value}.
	 * @see Stat#format(int)
	 */
	public final Component valueText;
	// ==================================================
	public SUGeneralStat(IStatsProvider statsProvider, Stat<ResourceLocation> stat)
	{
		super(statsProvider, id(stat), getGeneralStatText(stat));
		this.stat = Objects.requireNonNull(stat);
		this.value = statsProvider.getStatValue(stat);
		this.valueText = fLiteral(stat.format(this.value));
		this.isEmpty = this.value == 0;
	}
	// --------------------------------------------------
	private static final @Internal ResourceLocation id(Stat<ResourceLocation> stat)
	{
		//not the intended way of dealing with this, but has to be done to deal with incompatibilities,
		//and by incompatibilities i mean mod devs. not registering their stats because... idk either
		return Optional.ofNullable(BuiltInRegistries.CUSTOM_STAT.getKey(stat.getValue())).orElse(ID_NULL);
	}
	// ==================================================
	/**
	 * Returns the "general" {@link Stat} that corresponds with this {@link SUGeneralStat}.
	 */
	public final Stat<ResourceLocation> getGeneralStat() { return this.stat; }
	// --------------------------------------------------
	public final @Override boolean isEmpty() { return this.isEmpty; }
	// ==================================================
	/**
	 * Returns the translation key for a given {@link Stat}.
	 * @param stat The statistic in question.
	 */
	public static String getGeneralStatTranslationKey(Stat<ResourceLocation> stat)
	{
		//note: throw NullPointerException if stat is null
		return "stat." + stat.getValue().toString().replace(':', '.');
	}
	// --------------------------------------------------
	/**
	 * Returns the {@link Component} that should correspond to a
	 * given general {@link SUGeneralStat}, using {@link #getGeneralStatTranslationKey(Stat)}.
	 */
	public static Component getGeneralStatText(Stat<ResourceLocation> stat) { return fTranslatable(getGeneralStatTranslationKey(stat)); }
	// ==================================================
	/**
	 * Obtains a list of all "general" {@link Stat}s in form of {@link SUGeneralStat}.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUGeneralStat}s.
	 */
	public static List<SUGeneralStat> getGeneralStats
	(IStatsProvider statsProvider, @Nullable Predicate<SUGeneralStat> filter)
	{
		//null checks
		Objects.requireNonNull(statsProvider);
		
		//create an array list
		final var result = new ArrayList<SUGeneralStat>();
		
		//obtain stats
		final var statsList = new ObjectArrayList<Stat<ResourceLocation>>(Stats.CUSTOM.iterator());
		statsList.sort(Comparator.comparing(stat -> translatable(getGeneralStatTranslationKey(stat)).getString()));
		
		for(final var stat : statsList) result.add(new SUGeneralStat(statsProvider, stat));
		//filter out stats with NULL IDs and stats the filter filters out
		result.removeIf(stat -> Objects.equals(ID_NULL, stat.getStatID()) || (filter != null && !filter.test(stat)));
		
		//return the result list
		return result;
	}
	// ==================================================
}