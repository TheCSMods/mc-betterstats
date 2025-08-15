package io.github.thecsdev.betterstats.network;

import static io.github.thecsdev.betterstats.BetterStats.getModID;

import java.util.function.Function;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientPlayNetworkHandler;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.events.server.PlayerManagerEvent;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;

/**
 * Represents the server-side network handler for {@link BetterStats}.
 */
public final @Internal class BetterStatsNetwork
{
	// ==================================================
	private BetterStatsNetwork() {}
	// --------------------------------------------------
	public static final Component TXT_TOGGLE_TOOLTIP  = BST.net_toggleTooltip();
	public static final Component TXT_CONSENT_WARNING = BST.net_consentWarning();
	//
	public static final int NETWORK_VERSION = 3;
	//
	public static final ResourceLocation S2C_I_HAVE_BSS   = ResourceLocation.fromNamespaceAndPath(getModID(), "s2c_bss");
	public static final ResourceLocation C2S_I_HAVE_BSS   = ResourceLocation.fromNamespaceAndPath(getModID(), "c2s_bss");
	public static final ResourceLocation C2S_PREFERENCES  = ResourceLocation.fromNamespaceAndPath(getModID(), "c2s_prf");      //v3.11+ | NV 3+
	public static final ResourceLocation C2S_MCBS_REQUEST = ResourceLocation.fromNamespaceAndPath(getModID(), "c2s_mcbs_req"); //v3.11+ | NV 3+
	public static final ResourceLocation S2C_MCBS         = ResourceLocation.fromNamespaceAndPath(getModID(), "s2c_mcbs");     //v3.11+ | NV 3+
	// ==================================================
	public static void init() {}
	static
	{
		// ---------- SHORTCUT FUNCTIONS
		final Function<Player, BetterStatsClientPlayNetworkHandler> c = player ->
		BetterStatsClientPlayNetworkHandler.of((LocalPlayer)player);
		final Function<Player, BetterStatsServerPlayNetworkHandler> s = player ->
			BetterStatsServerPlayNetworkHandler.of((ServerPlayer)player);
		
		// ---------- SINGLEPLAYER/DEDICATED SERVER HANDLERS
		//init event handlers
		PlayerManagerEvent.PLAYER_CONNECTED.register(player ->
			s.apply(player).onPlayerConnected());
		
		//init network handlers
		CustomPayloadNetwork.registerReciever(PacketFlow.SERVERBOUND, C2S_I_HAVE_BSS, ctx ->
			s.apply(ctx.getPlayer()).onIHaveBss(ctx));
		
		CustomPayloadNetwork.registerReciever(PacketFlow.SERVERBOUND, C2S_PREFERENCES, ctx ->
			s.apply(ctx.getPlayer()).onPreferences(ctx));
		
		CustomPayloadNetwork.registerReciever(PacketFlow.SERVERBOUND, C2S_MCBS_REQUEST, ctx ->
			s.apply(ctx.getPlayer()).onMcbsRequest(ctx));
		
		// ---------- PURE CLIENT-SIDE HANDLERS
		if(BetterStats.isClient())
		{
			//init network handlers
			CustomPayloadNetwork.registerReciever(PacketFlow.CLIENTBOUND, S2C_I_HAVE_BSS, ctx ->
				c.apply(ctx.getPlayer()).onIHaveBss(ctx));
			
			CustomPayloadNetwork.registerReciever(PacketFlow.CLIENTBOUND, S2C_MCBS, ctx ->
				c.apply(ctx.getPlayer()).onMcbs(ctx));
		}
	}
	// ==================================================
}