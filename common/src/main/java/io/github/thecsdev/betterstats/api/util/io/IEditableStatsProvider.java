package io.github.thecsdev.betterstats.api.util.io;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.badge.PlayerBadge;

/**
 * An {@link IStatsProvider} whose statistics can be edited.
 * @apiNote Under no circumstance, make edits/changes to the {@link Minecraft#player}'s
 * {@link StatsCounter}, as that would result in a client/server de-sync.
 */
public interface IEditableStatsProvider extends IStatsProvider
{
	// ==================================================
	/**
	 * Sets the value of {@link IStatsProvider#getDisplayName()}.
	 * @param displayName The new "display name" for this {@link IStatsProvider}.
	 */
	public void setDisplayName(@Nullable Component displayName);
	
	/**
	 * Sets the value of {@link IStatsProvider#getGameProfile()}.
	 * @param playerProfile The new {@link GameProfile} value for this {@link IStatsProvider}.
	 */
	public void setGameProfile(@Nullable GameProfile playerProfile);
	// ==================================================
	/**
	 * Sets the value of a given {@link Stat}.
	 * @param stat The {@link Stat} whose value is to be changed.
	 * @param value The new {@link Stat} value.
	 * @apiNote This cannot be undone.
	 * @apiNote When overriding, make sure to enforce the legal range between `0` and `{@link Integer#MAX_VALUE}`.
	 */
	public void setStatValue(Stat<?> stat, int value) throws NullPointerException;
	
	/**
	 * Sets the value of a given {@link Stat}.
	 * @param type The {@link StatType}.
	 * @param stat The specific {@link Stat} associated with the {@link StatType}.
	 * @param value The new {@link Stat} value.
	 * @apiNote This cannot be undone.
	 * @apiNote You should not override this, as it calls {@link #setStatValue(Stat, int)} by default.
	 */
	default <T> void setStatValue(StatType<T> type, T stat, int value) throws NullPointerException
	{
		setStatValue(type.get(stat), value);
	}
	
	/**
	 * Increases the value of a given {@link Stat}.
	 * @param stat The {@link Stat} whose value is to be changed.
	 * @param value The {@link Stat} value increment.
	 * @apiNote This cannot be undone.
	 * @apiNote You should not override this, as it calls {@link #setStatValue(Stat, int)} by default.
	 */
	default void increaseStatValue(Stat<?> stat, int value)
	{
		final int i = (int)Math.min(/*must cast to long*/(long)getStatValue(stat) + value, Integer.MAX_VALUE);
		setStatValue(stat, i);
	}
	
	/**
	 * Increases the value of a given {@link Stat}.
	 * @param type The {@link StatType}.
	 * @param stat The specific {@link Stat} associated with the {@link StatType}.
	 * @param value The {@link Stat} value increment.
	 * @apiNote This cannot be undone.
	 * @apiNote You should not override this, as it calls {@link #increaseStatValue(Stat, int)} by default.
	 */
	default <T> void increaseStatValue(StatType<T> type, T stat, int value) { increaseStatValue(type.get(stat), value); }
	// ==================================================
	/**
	 * Sets the {@link Integer} value of a given {@link PlayerBadge} stat.
	 * @param badgeId The unique {@link ResourceLocation} of the {@link PlayerBadge}.
	 * @param value The new {@link Integer} value.
	 */
	public void setPlayerBadgeValue(ResourceLocation badgeId, int value) throws NullPointerException;
	
	/**
	 * Sets the {@link Integer} value of a given {@link PlayerBadge} stat.
	 * @param playerBadge The given {@link PlayerBadge}. Must be registered.
	 * @param value The new {@link Integer} value.
	 * @throws NullPointerException If the argument is {@code null}, or the {@link PlayerBadge} is not registered.
	 */
	default void setPlayerBadgeValue(PlayerBadge playerBadge, int value) throws NullPointerException
	{
		setPlayerBadgeValue(Objects.requireNonNull(playerBadge.getId().orElse(null)), value);
	}
	// ==================================================
	/**
	 * Copies all statistics present in another {@link RAMStatsProvider},
	 * and pastes them into this {@link IEditableStatsProvider}, overriding
	 * existing statistics in the process.
	 * @param from The {@link RAMStatsProvider} to copy stats from.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	default void setAll(RAMStatsProvider from) throws NullPointerException
	{
		Objects.requireNonNull(from);
		final var stm = from.getStatMap();
		final var pbm = from.getPlayerBadgeStatMap();
		stm.forEach((k, v) -> setStatValue(k, v));
		pbm.forEach((k, v) -> setPlayerBadgeValue(k, v));
	}
	// ==================================================
}