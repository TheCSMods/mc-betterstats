package io.github.thecsdev.betterstats;

import io.github.thecsdev.betterstats.util.stats.SASConfig;
import io.github.thecsdev.tcdcommons.api.config.AutoConfig;
import io.github.thecsdev.tcdcommons.api.config.annotation.NonSerialized;
import io.github.thecsdev.tcdcommons.api.config.annotation.SerializedAs;

public class BetterStatsConfig extends AutoConfig
{
	// ==================================================
	public static @NonSerialized boolean DEV_ENV         = false;
	// Temporary configurations bound to the current game session
	public static @NonSerialized boolean DEBUG_MODE      = false;
	public static @NonSerialized boolean SHOW_HUD_SCREEN = true;  //client-sided
	// --------------------------------------------------
	public @SerializedAs("client-guiSmoothScroll")        boolean guiSmoothScroll           = true;
	public @SerializedAs("client-guiMobsFollowCursor")    boolean guiMobsFollowCursor       = true;
	public @SerializedAs("client-trustAllServersBssNet")  boolean trustAllServersBssNet     = true;
	public @SerializedAs("client-allowStatsSharing")      boolean netPref_allowStatsSharing = false; //v3.11+
	public @SerializedAs("client-wideStatsPanel")         boolean wideStatsPanel            = false; //v3.12+
	public @SerializedAs("client-centeredStatsPanel")     boolean centeredStatsPanel        = false; //v3.12+
	public @SerializedAs("client-updateItemGroupsOnJoin") boolean updateItemGroupsOnJoin    = true;  //3.13.6
	public @SerializedAs("client-hidePlayerInfo")         boolean hidePlayerInfo            = false; //3.13.7
	public @SerializedAs("server-registerCommands")       boolean registerCommands          = true;
	public @SerializedAs("server-enableSAS")              boolean enableServerSAS           = false;
	public @SerializedAs("server-sasConfig")              SASConfig sasConfig = new SASConfig();
	// ==================================================
	public BetterStatsConfig(String name) { super(name); }
	// ==================================================
}