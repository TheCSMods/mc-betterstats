package io.github.thecsdev.betterstats.api.client.util.io;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.BetterStatsClient;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadgeHandler;
import io.github.thecsdev.tcdcommons.api.client.badge.ClientPlayerBadge;

/**
 * An {@link IStatsProvider} for {@link LocalPlayer}s.
 * @see Minecraft#player
 */
public final class LocalPlayerStatsProvider implements IStatsProvider
{
	// ==================================================
	private static LocalPlayerStatsProvider INSTANCE = null;
	// ==================================================
	private final LocalPlayer player;
	// --------------------------------------------------
	private final Component               displayName;
	private final GameProfile        gameProfile;
	private final StatsCounter        statsHandler;
	private final PlayerBadgeHandler badgeHandler;
	// ==================================================
	private LocalPlayerStatsProvider(LocalPlayer player) throws NullPointerException
	{
		this.player = Objects.requireNonNull(player);
		this.displayName  = player.getDisplayName();
		this.gameProfile  = player.getGameProfile();
		this.statsHandler = player.getStats();
		this.badgeHandler = ClientPlayerBadge.getClientPlayerBadgeHandler(player);
	}
	// --------------------------------------------------
	public final LocalPlayer getPlayer() { return this.player; }
	// ==================================================
	public final @Override Component getDisplayName() { return this.displayName; }
	public final @Override GameProfile getGameProfile() { return this.gameProfile; }
	// --------------------------------------------------
	public final @Override int getStatValue(Stat<?> stat) { return this.statsHandler.getValue(stat); }
	public final @Override <T> int getStatValue(StatType<T> type, T stat) { return this.statsHandler.getValue(type, stat); }
	public final @Override int getPlayerBadgeValue(ResourceLocation badgeId) { return this.badgeHandler.getValue(badgeId); }
	// ==================================================
	public final @Override int hashCode() { return this.player.hashCode(); }
	public final @Override boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final var lpsp = (LocalPlayerStatsProvider)obj;
		return (this.player == lpsp.player);
	}
	// ==================================================
	/**
	 * Returns the current {@link LocalPlayerStatsProvider} instance,
	 * or {@code null} if the {@link Minecraft} is not "in-game".
	 */
	public static final @Nullable LocalPlayerStatsProvider getInstance()
	{
		//obtain the Minecraft client instance
		final var player = BetterStatsClient.MC_CLIENT.player;
		//if the local player is null, return null...
		if(player == null) return (INSTANCE = null);
		//...else get or create instance
		else
		{
			//if the instance is null, or its stat handler no longer matches player stat handler,
			//create a new instance
			if(INSTANCE == null || INSTANCE.player != player)
				INSTANCE = new LocalPlayerStatsProvider(player);
			//finally return the instance
			return INSTANCE;
		}
	}
	
	/**
	 * Creates a {@link LocalPlayerStatsProvider} instance based on a {@link LocalPlayer}.
	 * @param player The {@link LocalPlayer}.
	 */
	public static final LocalPlayerStatsProvider of(LocalPlayer player) throws NullPointerException
	{
		//null-check
		Objects.requireNonNull(player);
		//return INSTANCE if the player is the same. create and return new stats provider otherwise
		return (INSTANCE != null && INSTANCE.player == player) ?
				INSTANCE : new LocalPlayerStatsProvider(player);
	}
	// ==================================================
}