package io.github.thecsdev.betterstats.neo;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.BetterStatsClient;
import io.github.thecsdev.betterstats.server.BetterStatsServer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(BetterStats.ModID)
public class BetterStatsNeo
{
	// ==================================================
	public BetterStatsNeo()
	{
		//create an instance of the mod's main class, depending on the dist
		switch(FMLEnvironment.dist)
		{
			case CLIENT           -> new BetterStatsClient();
			case DEDICATED_SERVER -> new BetterStatsServer();
		}
	}
	// ==================================================
}