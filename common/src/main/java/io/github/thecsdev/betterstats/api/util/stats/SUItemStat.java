package io.github.thecsdev.betterstats.api.util.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fTranslatable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import io.github.thecsdev.betterstats.api.util.BSUtils;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;

public final class SUItemStat extends SUStat<Item>
{
	// ==================================================
	protected final Item item;
	protected final @Nullable Block block;
	protected final boolean isEmpty; //cached value to avoid re-calculations
	// --------------------------------------------------
	public final int mined, crafted, used, broken, pickedUp, dropped;
	// ==================================================
	public SUItemStat(IStatsProvider statsProvider, Item item)
	{
		super(statsProvider, BuiltInRegistries.ITEM.getKey(Objects.requireNonNull(item)), getItemStatText(item));
		this.item = item;
		this.block = Block.byItem(item);
		
		//handle sMined
		if(this.block == null) mined = 0;
		else this.mined = statsProvider.getStatValue(Stats.BLOCK_MINED, this.block);
		
		//handle other stats
		this.crafted  = statsProvider.getStatValue(Stats.ITEM_CRAFTED, item);
		this.used     = statsProvider.getStatValue(Stats.ITEM_USED, item);
		this.broken   = statsProvider.getStatValue(Stats.ITEM_BROKEN, item);
		this.pickedUp = statsProvider.getStatValue(Stats.ITEM_PICKED_UP, item);
		this.dropped  = statsProvider.getStatValue(Stats.ITEM_DROPPED, item);
		
		//define isEmpty
		this.isEmpty = (this.mined == 0 && this.crafted == 0 &&
				this.used == 0 && this.broken == 0 &&
				this.pickedUp == 0 && this.dropped == 0);
	}
	// ==================================================
	/**
	 * Returns the {@link Item} corresponding with this {@link SUItemStat}.
	 */
	public final Item getItem() { return this.item; }
	
	/**
	 * Returns the {@link Block} that corresponds with {@link #getItem()},
	 * or {@code null} if the {@link Item} does not have a corresponding {@link Block}.
	 */
	public final @Nullable Block getBlock() { return this.block; }
	// --------------------------------------------------
	public final @Override boolean isEmpty() { return this.isEmpty; }
	// ==================================================
	/**
	 * Returns the {@link Component} that should correspond to a given {@link SUItemStat}.
	 */
	public static Component getItemStatText(Item item) { return fTranslatable(item.getDescriptionId()); }
	// ==================================================
	/**
	 * Obtains all {@link Item} {@link Stat}s in form of {@link SUItemStat}.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUItemStat}s.
	 */
	public static List<SUItemStat> getItemStats
	(IStatsProvider statsProvider, @Nullable Predicate<SUItemStat> filter)
	{
		//create the result list
		final var result = new ArrayList<SUItemStat>();
		
		//iterate and group all items
		for(final Item itemReg : BuiltInRegistries.ITEM)
		{
			//create item stat
			final var itemStat = new SUItemStat(statsProvider, itemReg);
			
			//filter
			if(filter != null && !filter.test(itemStat))
				continue;
			
			//add to the list
			result.add(itemStat);
		}
		
		//return the result
		return result;
	}
	
	/**
	 * Obtains all {@link Item} {@link Stat}s in form of {@link SUItemStat}, grouped
	 * into {@link CreativeModeTab}s using a {@link Map}.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUItemStat}s.
	 */
	public static Map<CreativeModeTab, List<SUItemStat>> getItemStatsByItemGroups
	(IStatsProvider statsProvider, @Nullable Predicate<SUItemStat> filter)
	{
		//create new map
		final var result = new LinkedHashMap<CreativeModeTab, List<SUItemStat>>();
		result.put(null, new ArrayList<>()); //the null category goes first
		
		//iterate and group all items
		for(final SUItemStat itemStat : getItemStats(statsProvider, filter))
		{
			//group item
			var itemRegGroup = BSUtils.getItemGroup(itemStat.item);
			if(!result.containsKey(itemRegGroup)) //ensure a corresponding list exists
				result.put(itemRegGroup, new ArrayList<>());
			result.get(itemRegGroup).add(itemStat);
		}
		
		//check the null category in case no items got listed there
		if(result.get(null).size() == 0)
			result.remove(null);
		
		//return the result
		return result;
	}
	
	/**
	 * Obtains all {@link Item} {@link Stat}s in form of {@link SUItemStat}, grouped
	 * into "mod groups" using a {@link Map}. The {@link Map} keys represent "mod IDs".
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUItemStat}s.
	 */
	public static Map<String, List<SUItemStat>> getItemStatsByModGroups
	(IStatsProvider statsProvider, @Nullable Predicate<SUItemStat> filter)
	{
		//create the result map
		final var result = new LinkedHashMap<String, List<SUItemStat>>();
		
		//add the 'minecraft' category first
		final String mcModId = ResourceLocation.DEFAULT_NAMESPACE;
		result.put(mcModId, new ArrayList<>());
		
		//iterate all items and add them to the map
		for(final SUItemStat itemStat : getItemStats(statsProvider, filter))
		{
			//---------- group the item
			//obtain mod id
			final String itemModId = itemStat.getStatID().getNamespace();
			
			//get or create a group array
			if(!result.containsKey(itemModId))
				result.put(itemModId, Lists.newArrayList());
			final var resultList = result.get(itemModId);
			
			//add the stat to the array
			resultList.add(itemStat);
		}
		
		//make sure 'minecraft' actually has entries
		//(aka handle cases where the filter filters out all 'minecraft' entries)
		if(result.get(mcModId).size() == 0)
			result.remove(mcModId);
		
		//return the result
		return result;
	}
	// --------------------------------------------------
	/**
	 * Same as {@link #getItemStatsByItemGroups(IStatsProvider, Predicate)},
	 * but the {@link Map} keys represent {@link Component}ual names of the {@link CreativeModeTab}s.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUItemStat}s.
	 */
	@Deprecated(since = "3.9.2", forRemoval = true)
	public static Map<Component, List<SUItemStat>> getItemStatsByItemGroupsB
	(IStatsProvider statsProvider, @Nullable Predicate<SUItemStat> filter)
	{
		return FilterGroupBy.DEFAULT.apply(getItemStats(statsProvider, filter));
	}
	
	/**
	 * Same as {@link #getItemStatsByModGroups(IStatsProvider, Predicate)},
	 * but the {@link Map} keys represent {@link Component}ual names of the mods.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUItemStat}s.
	 */
	@Deprecated(since = "3.9.2", forRemoval = true)
	public static Map<Component, List<SUItemStat>> getItemStatsByModGroupsB
	(IStatsProvider statsProvider, @Nullable Predicate<SUItemStat> filter)
	{
		return FilterGroupBy.MOD.apply(getItemStats(statsProvider, filter));
	}
	// ==================================================
}