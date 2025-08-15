package io.github.thecsdev.betterstats.api.util.enumerations;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.FiltersInitContext;
import io.github.thecsdev.betterstats.api.util.BSUtils;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.api.util.stats.SUStat;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;
import io.github.thecsdev.tcdcommons.api.util.io.mod.ModInfo;
import io.github.thecsdev.tcdcommons.api.util.io.mod.ModInfoProvider;

/**
 * A statistics filter {@link Enum} that dictates how statistics entries are to be grouped.
 * 
 * @see StatsTab
 * @see StatsTab#initFilters(FiltersInitContext)
 */
public enum FilterGroupBy implements ITextProvider
{
	// ==================================================
	/**
	 * The default grouping method, decided by the {@link StatsTab}.
	 */
	DEFAULT(BST.filter_groupBy_default()),
	
	/**
	 * Group all statistics entries into one single group.
	 */
	ALL(translatable("gui.all")),
	
	/**
	 * Group statistics entries based on the mod the entry's item belongs to.
	 */
	MOD(BST.filter_groupBy_mod());
	// ==================================================
	private final Component text;
	private FilterGroupBy(Component text) { this.text = Objects.requireNonNull(text); }
	public final @Override Component getText() { return this.text; }
	// ==================================================
	/**
	 * Groups a collection of {@link SUStat}s using this {@link FilterGroupBy}.
	 * @param stats The {@link SUStat}s to group.
	 * @param typeClassGetter Generic type getter. Leave this empty. Do not pass any arguments.
	 * @throws NullPointerException If an argument is null.
	 */
	public final @SafeVarargs <S extends SUStat<?>> Map<Component, List<S>> apply(
			Iterable<S> stats, S... typeClassGetter) throws NullPointerException
	{
		//requirements
		Objects.requireNonNull(stats);
		Objects.requireNonNull(typeClassGetter);
		
		//handle empty iterables
		if(!stats.iterator().hasNext())
			return new HashMap<Component, List<S>>();
		
		//do the grouping
		switch(this)
		{
			case ALL: return apply_all(stats);
			case MOD: return apply_mod(stats);
			case DEFAULT:
			default:
			{
				final var type = Objects.requireNonNull(typeClassGetter.getClass().getComponentType());
				if(type.equals(SUItemStat.class))
				{
					final @SuppressWarnings("unchecked") var statsI = (Iterable<SUItemStat>)stats;
					return apply_itemGroups(statsI);
				}
				else return apply_mod(stats);
			}
		}
	}
	// --------------------------------------------------
	private static final <S extends SUStat<?>> Map<Component, List<S>> apply_all(Iterable<S> stats)
	{
		//create a map...
		final var map = new HashMap<Component, List<S>>();
		//...put a single entry in it...
		//...and have the entry contain all the stats
		final var list = new ArrayList<S>(StreamSupport.stream(stats.spliterator(), false).toList());
		map.put(literal("*"), list);
		return map;
	}
	private static final <S extends SUStat<?>> Map<Component, List<S>> apply_mod(Iterable<S> stats)
	{
		final var mip = Objects.requireNonNull(ModInfoProvider.getInstance());
		
		//create the initial map
		final var map = new LinkedHashMap<String, List<S>>();
		
		//group all stats to their corresponding mod group
		StreamSupport.stream(stats.spliterator(), false).forEach(stat ->
		{
			//obtain the corresponding mod group
			final var groupName = stat.getStatID().getNamespace();
			var group = map.get(groupName);
			//ensure it exists. if not, create it
			if(group == null) map.put(groupName, group = new LinkedList<S>());
			//add the stat to the group
			group.add(stat);
		});
		
		//map the map's keys into Text-s, and return the result map
		return map.entrySet().stream().map(mapEntry ->
		{
			//obtain the mod info container for the given key
			final @Nullable ModInfo modInfo = mip.getModInfo(mapEntry.getKey());
			//construct a new key
			final var newKey = (modInfo != null) ? modInfo.getName() : literal(mapEntry.getKey());
			//construct a new map entry with the new key and the same value
			return new AbstractMap.SimpleEntry<>(newKey, mapEntry.getValue());
		})
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (u, v) -> u, LinkedHashMap::new));
	}
	@SuppressWarnings("unchecked")
	private static final <S extends SUStat<?>> Map<Component, List<S>> apply_itemGroups(Iterable<SUItemStat> stats)
	{
		//create the initial map, and the null group
		final var map = new LinkedHashMap<CreativeModeTab, List<S>>();
		map.put(null, new LinkedList<S>());
		
		//iterate all stats
		stats.forEach(stat ->
		{
			//obtain the item group
			final @Nullable var itemGroup = BSUtils.getItemGroup(stat.getItem());
			//obtain the map group, or create one if it doesn't exist
			var group = map.get(itemGroup);
			if(group == null) map.put(itemGroup, group = new LinkedList<S>());
			//put the item stat in the group
			group.add((S)stat);
		});
		
		//remove empty lists from the map
		map.entrySet().removeIf(e -> e.getValue().size() == 0);
		
		//map the map's keys, and return the resulting map
		return map.entrySet().stream().map(mapEntry ->
		{
			final var name = (mapEntry.getKey() != null) ?
					mapEntry.getKey().getDisplayName() :
					literal("*");
			return new AbstractMap.SimpleEntry<>(name, mapEntry.getValue());
		})
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (u, v) -> u, LinkedHashMap::new));
	}
	// ==================================================
}