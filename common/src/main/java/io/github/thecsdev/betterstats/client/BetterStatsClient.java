package io.github.thecsdev.betterstats.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.registry.BSClientPlayerBadges;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.util.BSUtils;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.events.client.MinecraftClientEvent;
import io.github.thecsdev.tcdcommons.api.events.client.gui.screen.GameMenuScreenEvent;
import io.github.thecsdev.tcdcommons.api.events.item.ItemGroupEvent;
import io.github.thecsdev.tcdcommons.api.hooks.client.gui.widget.ButtonWidgetHooks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.world.item.CreativeModeTabs;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

public final class BetterStatsClient extends BetterStats
{
	// ==================================================
	public static Minecraft MC_CLIENT; //auto-assigned convenience variable
	// --------------------------------------------------
	public static KeyMapping KEYBIND_TOGGLE_HUD;
	// ==================================================
	public BetterStatsClient()
	{
		//initialize and register stuff
		//register key-bindings
		KEYBIND_TOGGLE_HUD = new KeyMapping(
				BST.keybind_toggleHud(),
				InputConstants.UNKNOWN.getValue(),
				getModID());
		KeyMappingRegistry.register(KEYBIND_TOGGLE_HUD);

		BSStatsTabs.register();
		BSClientPlayerBadges.register();
		
		// ---------- modding the "Statistics" button
		//an event handler that will handle the game menu screen (the "pause" screen)
		GameMenuScreenEvent.INIT_WIDGETS_POST.register(gmScreen ->
		{
			//executing separately to really make sure the game menu screen finished initializing
			MC_CLIENT.execute(() ->
			{
				//easter egg - check the current date
				final var now = ZonedDateTime.now();
				if(now.getDayOfMonth() == 1 && now.getMonth() == Month.APRIL)
				{
					final var rn = new Random().nextInt(0, 101); //random number 0 to 100
					if(rn == 1) return; //randomly prevent `Better Statistics Screen` from opening
				}
				
				//locate the original stats button
				final Button ogStatsBtn = GuiUtils.findButtonWidgetOnScreen(gmScreen, translatable("gui.stats"));
				if(ogStatsBtn == null) return;
				
				//replace its function
				final var ogStatsBtn_onPress = ButtonWidgetHooks.getOnPress(ogStatsBtn);
				ButtonWidgetHooks.setOnPress(
					ogStatsBtn,
					btn ->
					{
						if(Screen.hasShiftDown()) ogStatsBtn_onPress.onPress(ogStatsBtn);
						else MC_CLIENT.setScreen(new BetterStatsScreen(MC_CLIENT.screen).getAsScreen());
					});
			});
		});
		
		// ---------- Performance optimizations
		//update the "Item to Group" map whenever item groups update
		ItemGroupEvent.UPDATE_DISPLAY_CONTEXT.register((a, b, c) -> BSUtils.updateITG());
		
		//pre-load dynamic content when joining worlds
		MinecraftClientEvent.JOINED_WORLD.register((client, world) ->
		{
			// ----------
			//do not do this if this feature is disabled
			if(!getConfig().updateItemGroupsOnJoin)
				return;
			// ----------
			//when the client joins a world, update the item group display context
			//right away, so as to avoid lag spikes when opening inventory later.
			//this is also here so this mod can display properly grouped items right away
			// ----------
			//update display context for items
			// From -> net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen @ lines 130, 170.
			CreativeModeTabs.tryRebuildTabContents(
					client.level.enabledFeatures(),
					client.options.operatorItemsTab().get() && client.player.canUseGameMasterBlocks(),
					world.registryAccess());
			
			//update the "Search" item group
			// From -> net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen @ line 173.
			if(client.player.connection.searchTrees() instanceof SessionSearchTrees sm)
			{
				final var list = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
				sm.updateCreativeTooltips(world.registryAccess(), list);
				sm.updateCreativeTags(list);
			}
			// ----------
		});
	}
	// ==================================================
}