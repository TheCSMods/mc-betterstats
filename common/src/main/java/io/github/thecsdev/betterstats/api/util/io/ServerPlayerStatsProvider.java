package io.github.thecsdev.betterstats.api.util.io;

import java.util.Objects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.badge.ServerPlayerBadgeHandler;

/**
 * An {@link IStatsProvider} that provides statistics
 * about a given {@link ServerPlayer}.
 */
public final class ServerPlayerStatsProvider implements IStatsProvider
{
	// ==================================================
	private final ServerPlayer player;
	// --------------------------------------------------
	private final Component                     displayName;
	private final GameProfile              gameProfile;
	private final ServerStatsCounter        statHandler;
	private final ServerPlayerBadgeHandler badgeHandler;
	// ==================================================
	private ServerPlayerStatsProvider(ServerPlayer player) throws NullPointerException
	{
		this.player       = Objects.requireNonNull(player);
		this.displayName  = player.getDisplayName();
		this.gameProfile  = player.getGameProfile();
		this.statHandler  = player.getStats();
		this.badgeHandler = ServerPlayerBadgeHandler.getServerBadgeHandler(player);
	}
	// --------------------------------------------------
	public final ServerPlayer getPlayer() { return this.player; }
	// ==================================================
	public final @Override Component getDisplayName() { return this.displayName; }
	public final @Override GameProfile getGameProfile() { return this.gameProfile; }
	// --------------------------------------------------
	public final @Override int getStatValue(Stat<?> stat) { return this.statHandler.getValue(stat); }
	public final @Override <T> int getStatValue(StatType<T> type, T stat) { return this.statHandler.getValue(type, stat); }
	public final @Override int getPlayerBadgeValue(ResourceLocation badgeId) { return this.badgeHandler.getValue(badgeId); }
	// ==================================================
	public final @Override int hashCode() { return this.player.hashCode(); }
	public final @Override boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final var spsp = (ServerPlayerStatsProvider)obj;
		return (this.player == spsp.player);
	}
	// ==================================================
	/**
	 * Creates a {@link ServerPlayerStatsProvider} instance based on a {@link ServerPlayer}.
	 * @param player The {@link LocalPlayer}.
	 */
	public static final ServerPlayerStatsProvider of(ServerPlayer player) throws NullPointerException
	{
		return new ServerPlayerStatsProvider(player);
	}
	// ==================================================
}