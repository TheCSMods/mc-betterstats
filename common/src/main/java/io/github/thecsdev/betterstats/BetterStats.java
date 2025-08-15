package io.github.thecsdev.betterstats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thecsdev.betterstats.command.StatisticsCommand;
import io.github.thecsdev.betterstats.network.BetterStatsNetwork;
import io.github.thecsdev.tcdcommons.TCDCommons;
import io.github.thecsdev.tcdcommons.api.events.server.command.CommandManagerEvent;
import io.github.thecsdev.tcdcommons.command.PlayerBadgeCommand;

public class BetterStats extends Object
{
	// ==================================================
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	private static final String ModName = "Better Statistics Screen";
	public  static final String ModID = "betterstats";
	private static BetterStats Instance;
	// --------------------------------------------------
	protected final BetterStatsConfig config;
	// ==================================================
	public BetterStats()
	{
		//validate instance first
		if(isModInitialized())
			throw new IllegalStateException(getModID() + " has already been initialized.");
		else if(!isInstanceValid(this))
			throw new UnsupportedOperationException("Invalid " + getModID() + " type: " + this.getClass().getName());
		
		//assign instance
		Instance = this;
		//modInfo = FabricLoader.getInstance().getModContainer(getModID()).get();
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
		
		//load config
		this.config = new BetterStatsConfig(getModID());
		this.config.loadFromFileOrCrash(true);
		
		//init stuff
		BetterStatsProperties.init();
		BetterStatsNetwork.init();

		// ---------- register commands
		CommandManagerEvent.COMMAND_REGISTRATION_CALLBACK.register((dispatcher, commandRegAccess, regEnv) ->
		{
			//check the config property 
			if(!this.config.registerCommands) return;
			
			//register commands
			StatisticsCommand.register(dispatcher, commandRegAccess);
			if(TCDCommons.getInstance().getConfig().enablePlayerBadges)
				PlayerBadgeCommand.register(dispatcher);
		});
	}
	// ==================================================
	public static BetterStats getInstance() { return Instance; }
	public BetterStatsConfig getConfig() { return this.config; }
	// --------------------------------------------------
	public static String getModName() { return ModName; }
	public static String getModID() { return ModID; }
	// --------------------------------------------------
	public static boolean isModInitialized() { return isInstanceValid(Instance); }
	private static boolean isInstanceValid(BetterStats instance) { return isServer(instance) || isClient(instance); }
	// --------------------------------------------------
	public static boolean isServer() { return isServer(Instance); }
	public static boolean isClient() { return isClient(Instance); }
	
	private static boolean isServer(BetterStats arg0) { return arg0 instanceof io.github.thecsdev.betterstats.server.BetterStatsServer; }
	private static boolean isClient(BetterStats arg0) { return arg0 instanceof io.github.thecsdev.betterstats.client.BetterStatsClient; }
	// ==================================================
}