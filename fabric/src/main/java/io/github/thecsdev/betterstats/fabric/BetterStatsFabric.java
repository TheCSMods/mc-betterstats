package io.github.thecsdev.betterstats.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * Fabric Mod Loader entry-points for this mod.
 */
public final class BetterStatsFabric implements ClientModInitializer, DedicatedServerModInitializer
{
	// ==================================================
	public @Override void onInitializeClient() { new io.github.thecsdev.betterstats.client.BetterStatsClient(); }
	public @Override void onInitializeServer() { new io.github.thecsdev.betterstats.server.BetterStatsServer(); }
	// ==================================================
}