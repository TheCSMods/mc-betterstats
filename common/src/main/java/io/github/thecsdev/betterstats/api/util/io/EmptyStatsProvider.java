package io.github.thecsdev.betterstats.api.util.io;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.util.TextUtils;

final @Internal class EmptyStatsProvider implements IStatsProvider
{
	// ==================================================
	private static final Component        NULL_NAME = TextUtils.literal("null");
	private static final GameProfile NULL_GP   = new GameProfile(new UUID(0, 0), "null");
	// ==================================================
	public EmptyStatsProvider() {}
	// ==================================================
	public final @Override int getStatValue(Stat<?> stat) { return 0; }
	public final @Override int getPlayerBadgeValue(ResourceLocation badgeId) { return 0; }
	public @Nullable GameProfile getGameProfile() { return NULL_GP; }
	public @Nullable Component getDisplayName() { return NULL_NAME; }
	// ==================================================
}