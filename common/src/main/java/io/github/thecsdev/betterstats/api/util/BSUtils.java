package io.github.thecsdev.betterstats.api.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class BSUtils
{
	// ==================================================
	/**
	 * An {@link Item} to {@link CreativeModeTab} map.
	 */
	private static final HashMap<Item, CreativeModeTab> ITG = new HashMap<Item, CreativeModeTab>();
	// ==================================================
	private BSUtils() {}
	// ==================================================
	/**
	 * Updates the {@link #ITG} {@link Map} that is used by {@link #getItemGroup(Item)}.
	 */
	public static final @Internal void updateITG()
	{
		//clear the ITG, and then update it
		ITG.clear();
		final var searchGroup = CreativeModeTabs.searchTab();
		final var air = Items.AIR;
		for(final CreativeModeTab group : CreativeModeTabs.allTabs())
		{
			//ignore the search group, as it is used for the
			//creative menu item search tab
			if(group == searchGroup) continue;
			
			//add group's items to ITG
			group.getDisplayItems().forEach(stack ->
			{
				//obtain the stack's item, and ensure an item is present
				//(in Minecraft's "language", AIR usually refers to "null")
				final var item = stack.getItem();
				if(item == null || item == air) return;
				
				//put the item and its group to the ITG map
				ITG.put(item, group);
			});
		}
	}
	// --------------------------------------------------
	/**
	 * Uses {@link #ITG} to find the {@link CreativeModeTab} for the given {@link Item}.
	 * @param item The {@link Item} in question.
	 * @apiNote An {@link Item} can be part of multiple {@link CreativeModeTab}s.
	 * This method will return the first or last found {@link CreativeModeTab}. Keep that in mind.
	 */
	public static @Nullable CreativeModeTab getItemGroup(Item item) { return ITG.getOrDefault(item, null); }
	// ==================================================
}