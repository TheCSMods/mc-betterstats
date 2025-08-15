package io.github.thecsdev.betterstats.api.util.interfaces;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.util.io.IEditableStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IStatsListener;
import net.minecraft.client.gui.screens.Screen;

/**
 * This interface is similar to {@link IStatsListener}.<br/>
 * The difference is that this interface is used to listen for "third-party"
 * statistics, aka things like statistics about other players that are
 * present on the server the client is currently connected to.
 * @see IStatsListener
 * @apiNote Intended to be used on the <b>client-side</b>.
 * @apiNote This interface is intended to be applied to client-side
 * {@link Screen}s. The reason this interface is present in common-sided
 * APIs is so {@link TpslContext} is fully visible in all contexts.
 */
public interface IThirdPartyStatsListener
{
	// ==================================================
	/**
	 * Called when third-party statistics are received.
	 * @param context The {@link TpslContext}.
	 */
	public void onStatsReady(TpslContext context);
	// ==================================================
	/**
	 * An interface that provides the context about received
	 * third-party statistics in {@link IThirdPartyStatsListener#onStatsReady(TpslContext)}.
	 */
	public static interface TpslContext
	{
		/**
		 * An enum that indicates what kind of third-party statistics were received.
		 * @apiNote Each {@link Type} has its own unique {@link Integer} value. This
		 * value must NOT be changed at all, as the BSS network depends on them being
		 * what they are currently set to.
		 */
		public static enum Type
		{
			/**
			 * Represents a "null"-like or "unknown" value.
			 * Used in places where actual {@code null} values are not allowed.
			 * @apiNote {@link TpslContext#getStatsProvider()} should return
			 * {@code null} when this {@link Type} applies.
			 */
			NULL(0),
			
			/**
			 * Indicates that the third-party statistics are about a player
			 * that is present on the same server the client is connected to.
			 */
			SAME_SERVER_PLAYER(100),
			
			/**
			 * Indicates that a request was made to retrieve statistics about
			 * another player present on the current server, but that other
			 * player is either offline or does not exist or does not consent
			 * to having their statistics shared.
			 * @apiNote {@link TpslContext#getStatsProvider()} should return {@code null}.
			 */
			SAME_SERVER_PLAYER_NOT_FOUND(101);
			
			private final int value;
			private Type(int value) { this.value = value; }
			public final int getIntValue() { return this.value; }
			
			public static final Type of(int intValue)
			{
				for(final var v : values())
					if(v.getIntValue() == intValue)
						return v;
				return NULL;
			}
		}
		
		/**
		 * Returns information about the {@link Type} of
		 * third-party statistics that were received.
		 */
		public Type getType();
		
		/**
		 * Returns the name of the player the {@link IStatsProvider} represents,
		 * if applicable. May not always be present.
		 */
		public @Nullable String getPlayerName();
		
		/**
		 * Returns the {@link IStatsListener} containing the third-party stats.
		 * @apiNote Depending on the {@link Type}, this may return {@code null}.
		 * @apiNote Might not be {@link IEditableStatsProvider}. Do not treat it as such.
		 */
		public @Nullable IStatsProvider getStatsProvider();
	}
	// ==================================================
}