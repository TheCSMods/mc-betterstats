package io.github.thecsdev.betterstats.util.stats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.thecsdev.tcdcommons.api.config.ACJsonHandler;

public class SASConfig implements ACJsonHandler<JsonObject>
{
	// ==================================================
	public String[] firstMinedBlocks = new String[] { "diamond_ore", "deepslate_diamond_ore", "ancient_debris", "deepslate_coal_ore", "dragon_egg", "sculk_sensor", "reinforced_deepslate" };
	public String[] firstCraftedItems = new String[] { "wooden_pickaxe", "diamond_pickaxe", "beacon", "netherite_block", "ender_eye" };
	public String[] firstKilledEntities = new String[] { "zombie", "blaze", "enderman", "ender_dragon", "warden", "wither", "player" };
	public String[] firstKilledByEntities = new String[] { "ender_dragon", "warden", "wither", "player" };
	public String[] firstCustomStats = new String[] { "deaths" };
	// ==================================================
	public final @Override JsonObject saveToJson()
	{
		final var json = new JsonObject();
		json.add("firstMinedBlocks", stringArrayToJsonArray(this.firstMinedBlocks));
		json.add("firstCraftedItems", stringArrayToJsonArray(this.firstCraftedItems));
		json.add("firstKilledEntities", stringArrayToJsonArray(this.firstKilledEntities));
		json.add("firstKilledByEntities", stringArrayToJsonArray(this.firstKilledByEntities));
		json.add("firstCustomStats", stringArrayToJsonArray(this.firstCustomStats));
		return json;
	}
	// --------------------------------------------------
	public final @Override boolean loadFromJson(JsonObject json)
	{
		//make an attempt to load the config; ignore failures
		try
		{
			if(json.has("firstMinedBlocks")) this.firstMinedBlocks = jsonArrayToStringArray(json.getAsJsonArray("firstMinedBlocks"));
			if(json.has("firstCraftedItems")) this.firstCraftedItems = jsonArrayToStringArray(json.getAsJsonArray("firstCraftedItems"));
			if(json.has("firstKilledEntities")) this.firstKilledEntities = jsonArrayToStringArray(json.getAsJsonArray("firstKilledEntities"));
			if(json.has("firstKilledByEntities")) this.firstKilledByEntities = jsonArrayToStringArray(json.getAsJsonArray("firstKilledByEntities"));
			if(json.has("firstCustomStats")) this.firstCustomStats = jsonArrayToStringArray(json.getAsJsonArray("firstCustomStats"));
			return true;
		}
		catch(Exception e) { return false; }
	}
	// ==================================================
	// Helper method to convert String array to JsonArray
	private final JsonArray stringArrayToJsonArray(String[] array)
	{
		JsonArray jsonArray = new JsonArray();
		for(final String item : array) jsonArray.add(item);
		return jsonArray;
	}

	// Helper method to convert JsonArray to String array
	private final String[] jsonArrayToStringArray(JsonArray jsonArray)
	{
		String[] array = new String[jsonArray.size()];
		for(int i = 0; i < jsonArray.size(); i++)
			array[i] = jsonArray.get(i).getAsString();
		return array;
	}
	// ==================================================
}