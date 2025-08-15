package io.github.thecsdev.betterstats.mixin.events;

import static io.github.thecsdev.betterstats.api.util.enumerations.ItemStatType.isItemStat;
import static io.github.thecsdev.betterstats.api.util.enumerations.MobStatType.isMobStat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.thecsdev.betterstats.network.BetterStatsServerPlayNetworkHandler;
import io.github.thecsdev.betterstats.util.stats.StatAnnouncementSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;

//super low priority is used to avoid conflicts with other mods, and to really make sure
//the code in these Mixin-s executes ONLY if the events are NOT cancelled by another mod
@Mixin(value = ServerStatsCounter.class, priority = -9000)
public abstract class MixinServerStatHandler extends StatsCounter
{
	@Inject(method = "setValue", at = @At("HEAD"))
	public void onPreSetStat(Player player, Stat<?> stat, int value, CallbackInfo ci)
	{
		//only handle server players
		if(player instanceof ServerPlayer sPlayer)
		{
			//handle SAS
			StatAnnouncementSystem.__handleStatChange(sPlayer, stat, stats.getInt(stat), value);
		}
	}
	
	@Inject(method = "setValue", at = @At("RETURN"))
	public void onSetStat(Player player, Stat<?> stat, int value, CallbackInfo ci)
	{
		//only handle server players
		if(player instanceof ServerPlayer serverPlayer)
		{
			//if a stat is an item stat or a mob stat..
			if(isItemStat(stat) || isMobStat(stat))
			{
				//..handle live stats updates for those stat types
				if(serverPlayer.getServer().isRunning())
					BetterStatsServerPlayNetworkHandler.of(serverPlayer).sendLiveStatsAttepmt();
			}
		}
	}
}