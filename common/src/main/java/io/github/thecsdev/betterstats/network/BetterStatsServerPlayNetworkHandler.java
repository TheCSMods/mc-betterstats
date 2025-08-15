package io.github.thecsdev.betterstats.network;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.NETWORK_VERSION;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.S2C_I_HAVE_BSS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.S2C_MCBS;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.util.interfaces.IThirdPartyStatsListener.TpslContext;
import io.github.thecsdev.betterstats.api.util.io.ServerPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.StatsProviderIO;
import io.github.thecsdev.tcdcommons.api.hooks.entity.EntityHooks;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetworkReceiver.PacketContext;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Better statistics screen server play network handler.<br/>
 * Keeps track of {@link ServerPlayer} data that is related to {@link BetterStats}.
 */
public final @Internal class BetterStatsServerPlayNetworkHandler
{
	// ==================================================
	/**
	 * The unique {@link ResourceLocation} for obtaining an instance of this network
	 * handler for a given player entity, via {@link EntityHooks#getCustomData(Entity)}.
	 */
	public static final ResourceLocation CUSTOM_DATA_ID = ResourceLocation.fromNamespaceAndPath(getModID(), "server_play_network_handler");
	// ==================================================
	private final ServerPlayer player;
	private final MinecraftServer    server;
	// --------------------------------------------------
	/**
	 * When set to {@code true}, this should never be switched back to {@code false}.<br/>
	 * Indicates whether or not the associated player has {@link BetterStats} installed.
	 */
	public boolean hasBssInstalled = false;
	// --------------------------------------------------
	/**
	 * When set to true, the {@link BetterStatsNetwork} will
	 * automatically update the client on their stats changes, live.
	 */
	public boolean netPref_enableLiveStats = false;
	
	/**
	 * The timestamp at which the last live stats update was performed.
	 * Used to avoid packet spam.
	 */
	public long liveStatsLastUpdate = 0;
	
	/**
	 * The {@link #player}'s preference on having their statistics
	 * shared with third-party sources such as other players.
	 */
	public boolean netPref_statsSharingConsent = false;
	// ==================================================
	private BetterStatsServerPlayNetworkHandler(ServerPlayer player) throws NullPointerException
	{
		this.player = Objects.requireNonNull(player);
		this.server = Objects.requireNonNull(player.getServer());
	}
	// --------------------------------------------------
	public final ServerPlayer getPlayer() { return this.player; }
	// ==================================================
	/**
	 * Handles the {@link #player} joining event.
	 * @apiNote Only executed once. No more, no less.
	 */
	public final void onPlayerConnected() { sendIHaveBss(); }
	
	/**
	 * Handles the {@link #player} letting this server know
	 * they have {@link BetterStats} installed.
	 */
	public final void onIHaveBss(PacketContext ctx)
	{
		//obtain the data buffer
		final var buffer = ctx.getPacketBuffer();
		
		//older mod versions don't write the network version. this is now unsupported
		if(buffer.readableBytes() == 0) return;
		
		//obtain network version and compare it
		final int netVer = buffer.readIntLE();
		if(netVer != NETWORK_VERSION) return;
		
		//update prefs
		this.hasBssInstalled = true;
	}
	
	/**
	 * Handles the {@link #player}'s preference changes for
	 * things like the "live hud stats updates" feature.
	 */
	public final void onPreferences(PacketContext ctx)
	{
		this.netPref_enableLiveStats     = ctx.getPacketBuffer().readBoolean();
		this.netPref_statsSharingConsent = ctx.getPacketBuffer().readBoolean();
	}
	
	/**
	 * Handles {@link #player}'s requests for MCBS files.
	 */
	public final void onMcbsRequest(PacketContext ctx)
	{
		//obtain the data buffer
		final var buffer = ctx.getPacketBuffer();
		
		//read and handle the packet type
		switch(TpslContext.Type.of(buffer.readInt()))
		{
			case SAME_SERVER_PLAYER:
			{
				//read requested player name, and obtain the player
				final var playerName = buffer.readUtf();
				final @Nullable var targetPlayer = this.server.getPlayerList().getPlayerByName(playerName);
				
				//if the requested player is offline, send null MCBS
				if(targetPlayer == null)
				{
					sendNullPlayerMcbs(playerName);
					return;
				}
				
				//send the MCBS
				if(!sendPlayerMcbs(targetPlayer, false)) //try to send target player's stats,
					sendNullPlayerMcbs(playerName);      //but if it fails, send null stats
			}
			break;
			default: break;
		}
	}
	// --------------------------------------------------
	/**
	 * Tells the {@link #player} that this server has {@link BetterStats} installed.
	 */
	public final void sendIHaveBss()
	{
		final var data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeIntLE(NETWORK_VERSION);
		CustomPayloadNetwork.sendS2C(this.player, S2C_I_HAVE_BSS, data);
	}
	
	/**
	 * Makes an attempt to send a live statistics update to the {@link #player}.
	 * The attempt may fail if the {@link #player} does not have {@link BetterStats}
	 * installed or if this method is being called too frequently.
	 */
	public final boolean sendLiveStatsAttepmt() //attempts to send live stats, if possible
	{
		//check prefs
		if(!this.netPref_enableLiveStats) return false;
		
		//check last update time, and avoid packet spam
		final long currentTime = System.currentTimeMillis();
		if(currentTime - this.liveStatsLastUpdate < 300) return false;
		
		//update last time, and send stats
		this.liveStatsLastUpdate = currentTime;
		this.player.getStats().sendStats(player);
		return true;
	}
	
	/**
	 * Sends an MCBS file of a given {@link ServerPlayer}, to the {@link #player}.
	 * @param targetPlayer The {@link ServerPlayer} whose MCBS is to be sent.
	 * @param force When {@code true}, this will ignore the {@link ServerPlayer} lack of consent.
	 */
	public final boolean sendPlayerMcbs(ServerPlayer targetPlayer, boolean force) throws NullPointerException
	{
		//check for consent
		Objects.requireNonNull(targetPlayer);
		if(!force && !BetterStatsServerPlayNetworkHandler.of(targetPlayer).netPref_statsSharingConsent)
			return false;
		
		//send
		final var data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeInt(TpslContext.Type.SAME_SERVER_PLAYER.getIntValue()); //write packet type (1 = player)
		data.writeUtf(targetPlayer.getName().getString());             //write player name
		StatsProviderIO.write(data, ServerPlayerStatsProvider.of(targetPlayer));
		CustomPayloadNetwork.sendS2C(this.player, S2C_MCBS, data);
		
		//return true to indicate success
		return true;
	}
	
	/**
	 * Sends an indicator that the MCBS for a given player is "null". The purpose of
	 * this is usually to let the client know that a given player's statistics cannot
	 * be shared because said player is currently offline.
	 * @see TpslContext.Type#SAME_SERVER_PLAYER_NOT_FOUND
	 */
	public final void sendNullPlayerMcbs(String playerName) throws NullPointerException
	{
		//requirements
		Objects.requireNonNull(playerName);
		
		//send
		final var data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeInt(TpslContext.Type.SAME_SERVER_PLAYER_NOT_FOUND.getIntValue());
		data.writeUtf(playerName);
		CustomPayloadNetwork.sendS2C(this.player, S2C_MCBS, data);
	}
	// ==================================================
	/**
	 * Returns an instance of {@link BetterStatsServerPlayNetworkHandler} for a given
	 * {@link ServerPlayer}. Creates one if it doesn't exist yet.
	 * @param player The {@link ServerPlayer}.
	 */
	public static final BetterStatsServerPlayNetworkHandler of(ServerPlayer player) throws NullPointerException
	{
		final var cd = EntityHooks.getCustomData(Objects.requireNonNull(player));
		@Nullable BetterStatsServerPlayNetworkHandler spnh = cd.getProperty(CUSTOM_DATA_ID);
		if(spnh == null)
		{
			spnh = new BetterStatsServerPlayNetworkHandler(player);
			cd.setProperty(CUSTOM_DATA_ID, spnh);
		}
		return spnh;
	}
	// ==================================================
}