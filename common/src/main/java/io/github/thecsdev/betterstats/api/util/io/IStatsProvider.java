package io.github.thecsdev.betterstats.api.util.io;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.badge.PlayerBadge;

/**
 * A component that contains loaded statistics about a given player.
 */
public interface IStatsProvider
{
	// ==================================================
	/**
	 * {@link EmptyStatsProvider} that always returns 0 for every single stat.
	 */
	public static final IStatsProvider EMPTY = new EmptyStatsProvider();
	// ==================================================
	/**
	 * Returns a "visual"/"user friendly" display {@link Component} that will
	 * be shown on the GUI screen as an indicator as to who the stats belong to.
	 * @apiNote Does not have to follow any Minecraft account naming rules or even correspond to one.
	 */
	public @Nullable Component getDisplayName();
	
	/**
	 * Returns the {@link GameProfile} of the player these stats belong to,
	 * or {@code null} if these stats are not associated with a player.
	 */
	public @Nullable GameProfile getGameProfile();
	// ==================================================
	/**
	 * Returns the {@link Integer} value of a given {@link Stat}.
	 * @param stat The {@link Stat} whose value is to be obtained.
	 * @see StatsCounter
	 */
	public int getStatValue(Stat<?> stat);
	
	/**
	 * Returns the {@link Integer} value of a given {@link StatType} and its corresponding {@link Stat}.
	 * @param type The {@link StatType}.
	 * @param stat The {@link Stat} whose value is to be obtained.
	 * @see StatsCounter
	 * @apiNote You should not override this, as it calls {@link #getStatValue(Stat)} by default.
	 */
	default <T> int getStatValue(StatType<T> type, T stat) { return type.contains(stat) ? getStatValue(type.get(stat)) : 0; }
	// --------------------------------------------------
	/**
	 * Returns the {@link Integer} value of a given {@link PlayerBadge} stat.
	 * @param badgeId The unique {@link ResourceLocation} of the {@link PlayerBadge}.
	 */
	public int getPlayerBadgeValue(ResourceLocation badgeId);
	
	/**
	 * Returns the {@link Integer} value of a given {@link PlayerBadge} stat.
	 * @param playerBadge The given {@link PlayerBadge}. Must be registered.
	 * @throws NullPointerException If the argument is {@code null}, or the {@link PlayerBadge} is not registered.
	 */
	default int getPlayerBadgeValue(PlayerBadge playerBadge) throws NullPointerException
	{
		return getPlayerBadgeValue(Objects.requireNonNull(playerBadge.getId().orElse(null)));
	}
	// ==================================================
}