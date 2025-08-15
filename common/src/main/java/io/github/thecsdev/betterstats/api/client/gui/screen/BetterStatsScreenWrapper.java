package io.github.thecsdev.betterstats.api.client.gui.screen;

import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.interfaces.IThirdPartyStatsListener;
import io.github.thecsdev.betterstats.client.network.OtherClientPlayerStatsProvider;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TDialogBoxScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.network.PlayerBadgeNetworkListener;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IStatsListener;

final @Internal class BetterStatsScreenWrapper
	extends TScreenWrapper<BetterStatsScreen>
	implements IStatsListener, PlayerBadgeNetworkListener, IThirdPartyStatsListener
{
	// ==================================================
	public BetterStatsScreenWrapper(BetterStatsScreen target) { super(target); }
	// ==================================================
	public final @Override void onStatsReady()
	{
		//if the user is viewing their own statistics, and they receive a statistics packet...
		if(this.target.getStatsProvider() == LocalPlayerStatsProvider.getInstance())
			//...refresh the statistics screen
			this.target.refresh();
	}
	
	public final @Override void onStatsReady(TpslContext context)
	{
		//only handle this if the screen is listening for these stats
		if(this.target.getStatsProvider() instanceof OtherClientPlayerStatsProvider tps &&
				Objects.equals(context.getPlayerName(), tps.getPlayerName()))
		{
			//handle based on response type
			switch(context.getType())
			{
				case SAME_SERVER_PLAYER: this.target.refresh(); break;
				case SAME_SERVER_PLAYER_NOT_FOUND:
					final var dialog = new TDialogBoxScreen(this,
							translatable("mco.configure.world.players.error"),
							BST.gui_tpsbs_ssps_playerNotFound());
					MC_CLIENT.setScreen(dialog.getAsScreen());
					break;
				default: break;
			}
		}
	}
	// --------------------------------------------------
	public @Override void onPlayerBadgesReady() { onStatsReady(); }
	// ==================================================
}