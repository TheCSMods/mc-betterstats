package io.github.thecsdev.betterstats.client.network;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.C2S_I_HAVE_BSS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.C2S_MCBS_REQUEST;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.C2S_PREFERENCES;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.NETWORK_VERSION;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.util.interfaces.IThirdPartyStatsListener;
import io.github.thecsdev.betterstats.api.util.interfaces.IThirdPartyStatsListener.TpslContext;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.StatsProviderIO;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.events.client.MinecraftClientEvent;
import io.github.thecsdev.tcdcommons.api.hooks.entity.EntityHooks;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetworkReceiver.PacketContext;
import io.github.thecsdev.tcdcommons.api.util.thread.TaskScheduler;
import io.netty.buffer.Unpooled;

/**
 * Better statistics screen client play network handler.<br/>
 * Keeps track of {@link LocalPlayer} data that is related to {@link BetterStats}.
 */
public final @Internal class BetterStatsClientPlayNetworkHandler
{
	// ==================================================
	/**
	 * The unique {@link ResourceLocation} for obtaining an instance of this network
	 * handler for a given player entity, via {@link EntityHooks#getCustomData(Entity)}.
	 */
	public static final ResourceLocation CUSTOM_DATA_ID = ResourceLocation.fromNamespaceAndPath(getModID(), "client_play_network_handler");
	// ==================================================
	private final LocalPlayer player;
	private final BetterStatsConfig  config;
	// --------------------------------------------------
	public boolean serverHasBss            = false;
	public boolean bssNetworkConsent       = false;
	public boolean netPref_enableLiveStats = false;
	// --------------------------------------------------
	private final Cache<String, OtherClientPlayerStatsProvider> sessionPlayerStatStorage;
	// ==================================================
	private BetterStatsClientPlayNetworkHandler(LocalPlayer player) throws NullPointerException
	{
		//assign values
		this.player = Objects.requireNonNull(player);
		this.config = Objects.requireNonNull(BetterStats.getInstance().getConfig());
		this.sessionPlayerStatStorage = CacheBuilder.newBuilder()
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.build();
		TaskScheduler.schedulePeriodicCacheCleanup(this.sessionPlayerStatStorage);
		
		//disconnection handler
		//note: must execute only once. must unregister itself after execution
		final AtomicReference<MinecraftClientEvent.ClientDisconnect> evhCd = new AtomicReference<>(null);
		evhCd.set(client ->
		{
			MinecraftClientEvent.DISCONNECTED.unregister(Objects.requireNonNull(evhCd.get()));
			onDisconnected();
		});
		MinecraftClientEvent.DISCONNECTED.register(Objects.requireNonNull(evhCd.get()));
	}
	// --------------------------------------------------
	public final LocalPlayer getPlayer() { return this.player; }
	// --------------------------------------------------
	/**
	 * Obtains the {@link OtherClientPlayerStatsProvider} from the
	 * {@link #sessionPlayerStatStorage}, creating an instance if it doesn't exist yet.
	 */
	public final OtherClientPlayerStatsProvider getSessionPlayerStats(String otherPlayerName)
		throws NullPointerException
	{
		Objects.requireNonNull(otherPlayerName);
		@Nullable var stats = this.sessionPlayerStatStorage.getIfPresent(otherPlayerName);
		if(stats == null)
		{
			stats = new OtherClientPlayerStatsProvider(otherPlayerName);
			this.sessionPlayerStatStorage.put(otherPlayerName, stats);
		}
		return stats;
	}
	// ==================================================
	/**
	 * Handles the {@link #player} disconnecting from the server.
	 */
	public final void onDisconnected()
	{
		//clear flags from the no-longer-valid session
		this.serverHasBss            = false;
		this.netPref_enableLiveStats = false;
		this.bssNetworkConsent       = false;
		this.sessionPlayerStatStorage.invalidateAll();;
	}
	
	/**
	 * Handles the server telling the {@link #player} it has {@link BetterStats} installed.
	 */
	public final void onIHaveBss(PacketContext ctx)
	{
		//ignore duplicate packets
		if(this.serverHasBss) return;
		
		//obtain data buffer and make sure data is present
		final var buffer = ctx.getPacketBuffer();
		if(buffer.readableBytes() == 0) return;
		
		//obtain network version and compare it
		final int netVer = buffer.readIntLE();
		if(netVer != NETWORK_VERSION) return; //ignore incompatible servers
		
		//server has BSS
		this.serverHasBss = true;
		LOGGER.info("[Client] The server has '" + getModID() + "' installed.");
		
		//if the client is in single player, handle live stats updates
		//in case the statistics hud had entries in it
		if(MC_CLIENT.isLocalServer() || BetterStats.getInstance().getConfig().trustAllServersBssNet)
			TaskScheduler.executeOnce(MC_CLIENT, () -> this.player.connection != null, () ->
			{
				sendIHaveBss(true);
				sendPreferences();
			});
	}
	
	/**
	 * Handles the server sending the {@link #player} an MCBS file.
	 */
	public final void onMcbs(PacketContext ctx)
	{
		//obtain the buffer
		final var buffer = ctx.getPacketBuffer();
		
		//utility function for obtaining listener screens
		//(NOTE: MUST be executed on the MAIN/RENDER thread!)
		final Supplier<@Nullable IThirdPartyStatsListener> listenerSupplier = () ->
		{
			@Nullable IThirdPartyStatsListener listener = null;
			if(MC_CLIENT.screen instanceof IThirdPartyStatsListener l)
				listener = l;
			else if(MC_CLIENT.screen instanceof TScreenWrapper<?> tsw &&
					tsw.getTargetTScreen() instanceof IThirdPartyStatsListener l)
				listener = l;
			return listener;
		};
		
		//read and handle the packet type
		switch(TpslContext.Type.of(buffer.readInt()))
		{
			case NULL:
			{
				//the following must be done on Minecraft's main thread
				MC_CLIENT.executeIfPossible(() ->
				{
					@Nullable IThirdPartyStatsListener listener = listenerSupplier.get();
					if(listener != null)
						listener.onStatsReady(new TpslContext()
						{
							public Type getType() { return TpslContext.Type.NULL; }
							public String getPlayerName() { return null; }
							public IStatsProvider getStatsProvider() { return null; }
						});
				});
			}
			break;
			case TpslContext.Type.SAME_SERVER_PLAYER:
			{
				//read player name
				final var playerName = buffer.readUtf();
				
				//read MCBS
				final var tempStatsProvider = new OtherClientPlayerStatsProvider(playerName);
				try { StatsProviderIO.read(buffer, tempStatsProvider); }
				catch(Exception exc) {/*ignore failures to process the MCBS file*/}
				
				//store MCBS
				final var spss = getSessionPlayerStats(playerName);
				spss.setAll(tempStatsProvider);
				
				//the following must be done on Minecraft's main thread
				MC_CLIENT.executeIfPossible(() ->
				{
					@Nullable IThirdPartyStatsListener listener = listenerSupplier.get();
					if(listener != null)
						listener.onStatsReady(new TpslContext()
						{
							public Type getType() { return TpslContext.Type.SAME_SERVER_PLAYER; }
							public String getPlayerName() { return playerName; }
							public IStatsProvider getStatsProvider() { return spss; }
						});
				});
			}
			break;
			case SAME_SERVER_PLAYER_NOT_FOUND:
			{
				//read player name
				final var playerName = buffer.readUtf();
				
				//the following must be done on Minecraft's main thread
				MC_CLIENT.executeIfPossible(() ->
				{
					@Nullable IThirdPartyStatsListener listener = listenerSupplier.get();
					if(listener != null)
						listener.onStatsReady(new TpslContext()
						{
							public Type getType() { return TpslContext.Type.SAME_SERVER_PLAYER_NOT_FOUND; }
							public String getPlayerName() { return playerName; }
							public @Nullable IStatsProvider getStatsProvider() { return null; }
						});
				});
			}
			break;
			default: break;
		}
	}
	// --------------------------------------------------
	/**
	 * Returns {@code true} if {@link BetterStatsClientPlayNetworkHandler}
	 * is allowed to communicate with the server.
	 */
	public final boolean comms() { return MC_CLIENT.isLocalServer() || (this.serverHasBss && this.bssNetworkConsent); }
	
	/**
	 * Sends the server a message letting the server know the
	 * {@link #player} has {@link BetterStats} installed.
	 */
	public final boolean sendIHaveBss(boolean forceSend)
	{
		//check if can send
		if(!forceSend && !comms()) return false;
		this.bssNetworkConsent = true; //if force-sent, then we are likely in single-player
		
		//construct and send
		final var data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeIntLE(NETWORK_VERSION);
		CustomPayloadNetwork.sendC2S(C2S_I_HAVE_BSS, data);
		return true;
	}
	
	/**
	 * Sends the player's preferences to the server, such as for
	 * example the "enable live stats" preference.
	 */
	public final boolean sendPreferences()
	{
		//if communications are off, don't send
		if(!comms()) return false;
		
		//construct and send
		final var data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeBoolean(this.       netPref_enableLiveStats);   //write live stats preference
		data.writeBoolean(this.config.netPref_allowStatsSharing); //write stats sharing consent preference
		CustomPayloadNetwork.sendC2S(C2S_PREFERENCES, data);
		
		//return true to indicate success
		return true;
	}
	
	/**
	 * Similar to {@link #sendPreferences()}, except the sent preferences disable
	 * everything, and the {@link #bssNetworkConsent} is fully revoked.
	 */
	public final boolean sendAndRevokePreferences()
	{
		//if communications are off, don't send
		if(!comms()) return false;
		
		//construct and send
		final var data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeBoolean(false); //write live stats preference
		data.writeBoolean(false); //write stats sharing consent preference
		CustomPayloadNetwork.sendC2S(C2S_PREFERENCES, data);
		
		//assign new state value, and return true to indicate success
		this.netPref_enableLiveStats   = false;
		this.bssNetworkConsent = false;
		return true;
	}
	
	/**
	 * Sends a third-party player statistics request to the server.
	 * The server should respond by sending back the given player's
	 * statistics, if said player consents to this.
	 * @param playerName The name of the other player whose statistics this client wishes to see.
	 */
	public final boolean sendMcbsRequest(String playerName)
	{
		//requirements
		Objects.requireNonNull(playerName);
		
		//if communications are off, don't send
		if(!comms()) return false;
		
		//construct and send
		final var data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeInt(TpslContext.Type.SAME_SERVER_PLAYER.getIntValue()); //write request type (1 = player)
		data.writeUtf(playerName);                                     //write player name
		CustomPayloadNetwork.sendC2S(C2S_MCBS_REQUEST, data);
		
		//return true to indicate success
		return true;
	}
	// ==================================================
	/**
	 * Returns an instance of {@link BetterStatsClientPlayNetworkHandler} from a given
	 * {@link LocalPlayer}. Creates one if it doesn't exist yet.
	 * @param player The {@link LocalPlayer}.
	 */
	public static final BetterStatsClientPlayNetworkHandler of(LocalPlayer player) throws NullPointerException
	{
		final var cd = EntityHooks.getCustomData(Objects.requireNonNull(player));
		@Nullable BetterStatsClientPlayNetworkHandler cpnh = cd.getProperty(CUSTOM_DATA_ID);
		if(cpnh == null)
		{
			cpnh = new BetterStatsClientPlayNetworkHandler(player);
			cd.setProperty(CUSTOM_DATA_ID, cpnh);
		}
		return cpnh;
	}
	
	/**
	 * Returns an instance of {@link BetterStatsClientPlayNetworkHandler} for
	 * {@link Minecraft#player}, or {@code null} if the client player is also {@code null}.
	 */
	public static final @Nullable BetterStatsClientPlayNetworkHandler getInstance()
	{
		final var player = MC_CLIENT.player;
		final var network = MC_CLIENT.getConnection();
		if(player == null || network == null) return null; else return of(player);
	}
	// ==================================================
}