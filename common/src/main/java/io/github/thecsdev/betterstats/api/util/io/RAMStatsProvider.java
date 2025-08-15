package io.github.thecsdev.betterstats.api.util.io;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.util.exceptions.UnsupportedFileVersionException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * An {@link IEditableStatsProvider} whose statistics are loaded into and
 * held in the memory (aka RAM), hence the name {@link RAMStatsProvider}.
 * @apiNote Extending this class is not recommended. This class is not final
 * only because an {@link Internal} component needs to extend it.
 */
public /*non-final*/ class RAMStatsProvider implements IEditableStatsProvider
{
	// ==================================================
	protected @Nullable Component displayName;
	protected @Nullable GameProfile gameProfile;
	// --------------------------------------------------
	protected final Object2IntMap<Stat<?>> statMap = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());
	protected final Object2IntMap<ResourceLocation> playerBadgeStatMap = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());
	// ==================================================
	public RAMStatsProvider()
	{
		this.statMap.defaultReturnValue(0);
		this.playerBadgeStatMap.defaultReturnValue(0);
	}
	
	/**
	 * Creates a {@link RAMStatsProvider} instance, after which
	 * {@link StatsProviderIO#read(FriendlyByteBuf, IEditableStatsProvider)} is called.
	 * @param buffer The {@link FriendlyByteBuf} to read from.
	 * @param releaseBuffer After reading, call {@link FriendlyByteBuf#release()}?
	 * @apiNote {@link FriendlyByteBuf#release()} will get called when requested, even when an {@link Exception} is raised.
	 */
	public RAMStatsProvider(FriendlyByteBuf buffer, boolean releaseBuffer)
			throws NullPointerException, IllegalHeaderException, UnsupportedFileVersionException
	{
		this();
		try { StatsProviderIO.read(Objects.requireNonNull(buffer), this); }
		finally { if(releaseBuffer && buffer.refCnt() > 0) buffer.release(); }
	}
	// ==================================================
	public final @Override Component getDisplayName() { return this.displayName; }
	public final @Override void setDisplayName(Component displayName)
	{
		if(displayName == null) displayName = literal("-");
		this.displayName = displayName;
	}
	//
	public final @Override GameProfile getGameProfile() { return this.gameProfile; }
	public final @Override void setGameProfile(@Nullable GameProfile playerProfile) { this.gameProfile = playerProfile; }
	// --------------------------------------------------
	public final @Override int getStatValue(Stat<?> stat) { return this.statMap.getInt(stat); }
	public final @Override void setStatValue(Stat<?> stat, int value) throws NullPointerException
	{
		if(value < 1) this.statMap.removeInt(stat);
		else this.statMap.put(Objects.requireNonNull(stat), value);
	}
	// --------------------------------------------------
	public final @Override int getPlayerBadgeValue(ResourceLocation badgeId) { return this.playerBadgeStatMap.getInt(badgeId); }
	public final @Override void setPlayerBadgeValue(ResourceLocation badgeId, int value) throws NullPointerException
	{
		if(value < 1) this.playerBadgeStatMap.removeInt(badgeId);
		else this.playerBadgeStatMap.put(Objects.requireNonNull(badgeId), value);
	}
	// ==================================================
	public final Object2IntMap<Stat<?>> getStatMap() { return this.statMap; }
	public final Object2IntMap<ResourceLocation> getPlayerBadgeStatMap() { return this.playerBadgeStatMap; }
	// ==================================================
}