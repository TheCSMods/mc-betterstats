package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.api.util.io.StatsProviderIO.FILE_EXTENSION;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement.COLOR_BACKGROUND;

import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.io.mod.ModInfoProvider;

/**
 * Base for {@link QuickShareUploadScreen} and {@link QuickShareDownloadScreen}.
 */
abstract class QuickShareScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	protected static final String QSC_SUFFIX = ("." + FILE_EXTENSION).toLowerCase(Locale.ENGLISH);
	// --------------------------------------------------
	private @Nullable Screen parent;
	// ==================================================
	public QuickShareScreen(@Nullable Screen parent, Component title)
	{
		super(title);
		this.parent = parent;
	}
	// --------------------------------------------------
	public final @Override Screen getParentScreen() { return this.parent; }
	public @Virtual @Override void close() { MC_CLIENT.setScreen(getParentScreen()); }
	// ==================================================
	protected final void refresh() { MC_CLIENT.executeIfPossible(() -> { if(!isOpen()) return; clearChildren(); init(); }); }
	// --------------------------------------------------
	public @Virtual @Override void renderBackground(TDrawContext pencil)
	{
		super.renderBackground(pencil);
		pencil.drawTFill(COLOR_BACKGROUND);
	}
	// ==================================================
	/**
	 * Adds additional information to {@link JsonObject}s that act
	 * as HTTP request bodies for requests send to BSS APIs.<br>
	 * This information helps the BSS servers respond accordingly, as well
	 * helping prevent abuse and enforce limitations.<br>
	 * Please see the Privacy Policy document for more info.
	 */
	protected static final @Internal void addTelemetryData(JsonObject httpRequestBody)
	{
		//information used for feature functionality.
		//the feature may function differently on different versions, so the server has
		//to know what versions the user is running, so the server can respond properly 
		final var mi            = Objects.requireNonNull(ModInfoProvider.getInstance());
		final var miMinecraft   = mi.getModInfo("minecraft");
		final var miBetterStats = mi.getModInfo(getModID());
		
		httpRequestBody.addProperty("mod_info.minecraft.version",   miMinecraft.getVersion());
		httpRequestBody.addProperty("mod_info.betterstats.version", miBetterStats.getVersion());
		
		//information used for security purposes and limitation enforcement.
		//it is unfortunate that trying to give back to the community by offering a free and
		//privacy-friendly feature will almost always mean someone is gonna try and abuse the system and
		//cause significant damage, forcing developers to be a bit more "invasive" to protect themselves.
		//despite these obstacles, i will try my best to be as privacy-friendly, while still keeping
		//everyone safe. please refer to the privacy-policy documents on my website for more info.
		//the following lines were added in v3.12.6:
		final var session      = MC_CLIENT.getUser();
		final var session_uuid = Optional.ofNullable(session.getProfileId()).orElse(new UUID(0,0));
		
		httpRequestBody.addProperty("session.username",     session.getName());
		httpRequestBody.addProperty("session.uuid",         session_uuid.toString());
		httpRequestBody.addProperty("session.account_type", switch(session.getType())
		{
			case LEGACY -> "legacy";
			case MOJANG -> "mojang";
			case MSA    -> "microsoft";
			default     -> "unknown";
		});
		httpRequestBody.add("device.net.mac_address", getNicMacAddr());
	}
	// --------------------------------------------------
	//get network interface card media access control address
	private static final @Internal JsonElement getNicMacAddr()
	{
		try
		{
			//obtain the local address
			final var socket    = new Socket("example.com", 443);
			final var localAddr = socket.getLocalAddress();
			socket.close();
			
			//obtain the network interface card's MAC address
			final var netInt  = Objects.requireNonNull(NetworkInterface.getByInetAddress(localAddr));
			final var netAddr = Objects.requireNonNull(netInt.getHardwareAddress());
			
			final var netAddrJson = new JsonArray();
			IntStream.range(0, netAddr.length).map(i -> netAddr[i]).forEach(i -> netAddrJson.add(i));
			
			//write the MAC
			return netAddrJson;
		}
		catch(Exception e) { return JsonNull.INSTANCE; }
	}
	// ==================================================
}