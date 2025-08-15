package io.github.thecsdev.betterstats.client.mixin.events;

import io.github.thecsdev.betterstats.client.BetterStatsClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraftClient
{
	// ==================================================
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(CallbackInfo callback)
	{
		//assign the minecraft client instance once it's ready
		BetterStatsClient.MC_CLIENT = (Minecraft)(Object)this;
	}
	// ==================================================
}
