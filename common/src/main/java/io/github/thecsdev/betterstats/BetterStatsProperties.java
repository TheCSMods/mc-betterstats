package io.github.thecsdev.betterstats;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.google.gson.JsonObject;

import io.github.thecsdev.tcdcommons.TCDCommons;

/**
 * Properties from the <code>betterstats.properties.json</code> file.
 */
@Internal
public final class BetterStatsProperties
{
	// ==================================================
	private static final JsonObject MOD_PROPERTIES;
	// --------------------------------------------------
	public static final String URL_SOURCES, URL_ISSUES, URL_CURSEFORGE, URL_MODRINTH;
	public static final String URL_WEBSITE, URL_YOUTUBE;
	public static final String URL_QS_LEGAL, URL_REMOTE_APIS;
	// ==================================================
	public static final void init() {/*calls static*/}
	static
	{
		//read the mod properties resource file
		try
		{
			final var propertiesStream = BetterStats.class.getResourceAsStream("/betterstats.properties.json");
			final var propertiesJsonStr = new String(propertiesStream.readAllBytes());
			propertiesStream.close();
			MOD_PROPERTIES = TCDCommons.GSON.fromJson(propertiesJsonStr, JsonObject.class);
		}
		catch(Exception e) { throw new ExceptionInInitializerError(e); }
		
		//read links
		final var links = MOD_PROPERTIES.get("links").getAsJsonObject();
		URL_SOURCES     = links.get("sources")         .getAsString();
		URL_ISSUES      = links.get("issues")          .getAsString();
		
		URL_CURSEFORGE  = links.get("curseforge")      .getAsString();
		URL_MODRINTH    = links.get("modrinth")        .getAsString();
		URL_WEBSITE     = links.get("website")         .getAsString();
		URL_YOUTUBE     = links.get("youtube")         .getAsString();
		
		URL_QS_LEGAL    = links.get("quickshare_legal").getAsString();
		URL_REMOTE_APIS = links.get("remote_api_links").getAsString();
	}
	// ==================================================
	public static final @Internal JsonObject getModProperties() { return MOD_PROPERTIES; }
	// ==================================================
}