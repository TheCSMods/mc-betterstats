package io.github.thecsdev.betterstats.api.client.gui.screen;

import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel.FILTER_ID_SCROLL_CACHE;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.StatsProviderIO;
import io.github.thecsdev.betterstats.client.BetterStatsClient;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientPlayNetworkHandler;
import io.github.thecsdev.betterstats.client.network.OtherClientPlayerStatsProvider;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;

/**
 * The main focal point of this mod.<br/>
 * The screen where player statistics are shown, but better.
 */
public final class BetterStatsScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	private final @Nullable Screen parent;
	// --------------------------------------------------
	private final     IStatsProvider     statsProvider;
	private @Nullable StatsTab           selectedStatsTab = BSStatsTabs.GENERAL;
	private final     StatFilterSettings filterSettings   = new StatFilterSettings();
	// --------------------------------------------------
	private boolean statsAlreadyRequested = false; //prevents duplicate requests and soft-locks
	// --------------------------------------------------
	private @Nullable BetterStatsPanel bsPanel;
	// ==================================================
	/**
	 * Constructs the {@link BetterStatsScreen} using the {@link LocalPlayerStatsProvider}.
	 * @param parent The {@link Screen} that should open when this one closes.
	 * @throws NullPointerException If The {@link LocalPlayerStatsProvider} is "unavailable" aka {@code null}.
	 * @see LocalPlayerStatsProvider#getInstance()
	 */
	public BetterStatsScreen(@Nullable Screen parent) throws NullPointerException { this(parent, LocalPlayerStatsProvider.getInstance()); }
	
	/**
	 * Constructs the {@link BetterStatsScreen} using a custom {@link IStatsProvider}.
	 * @param parent The {@link Screen} that should open when this one closes.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @throws NullPointerException If the {@link IStatsProvider} is {@code null}.
	 */
	public BetterStatsScreen(@Nullable Screen parent, IStatsProvider statsProvider) throws NullPointerException
	{
		super(translatable("gui.stats"));
		this.parent = parent;
		this.statsProvider = Objects.requireNonNull(statsProvider);
	}
	
	protected final @Override TScreenWrapper<?> createScreenWrapper() { return new BetterStatsScreenWrapper(this); }
	public final @Override Screen getParentScreen() { return this.parent; }
	// ==================================================
	public final @Override boolean shouldPause() { return true; }
	public final @Override boolean shouldRenderInGameHud() { return false; }
	public final @Override void close() { getClient().setScreen(this.parent); }
	// --------------------------------------------------
	protected final @Override void onOpened()
	{
		//prevent duplicate requests and soft-locks
		if(this.statsAlreadyRequested) return;
		this.statsAlreadyRequested = true;
		
		//request the statistics
		try
		{
			//return if the connection or the local player are not available
			if(this.client.player == null || this.client.player.connection == null)
				return;
			
			//send a statistics request packet if the client is viewing their own stats
			if(this.statsProvider == LocalPlayerStatsProvider.getInstance())
				this.client.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
			
			//send a third-party statistics request if viewing another player's stats
			else if(this.statsProvider instanceof OtherClientPlayerStatsProvider ssps)
				BetterStatsClientPlayNetworkHandler.getInstance().sendMcbsRequest(ssps.getPlayerName());
		}
		catch(Exception exc) {/*ignore connectivity exceptions*/}
	}
	protected final @Override void onClosed()
	{
		//caching scroll amount, in case this screen is re-opened later
		double val = this.bsPanel.getStatsTabVerticalScrollAmount();
		this.filterSettings.setProperty(FILTER_ID_SCROLL_CACHE, val);
	}
	// ==================================================
	/**
	 * Returns the {@link IStatsProvider} associated with this {@link BetterStatsScreen}.
	 */
	public final IStatsProvider getStatsProvider() { return this.statsProvider; }
	
	/**
	 * Returns the currently selected {@link StatsTab}.
	 */
	public final @Nullable StatsTab getStatsTab() { return this.selectedStatsTab; }
	
	/**
	 * Sets the currently selected {@link StatsTab}, after which {@link #refresh()} is called.
	 * @param statsTab The {@link StatsTab} to display.
	 */
	public final void setStatsTab(@Nullable StatsTab statsTab) { this.selectedStatsTab = statsTab; refresh(); }
	// ==================================================
	/**
	 * Refreshes this screen by clearing and re-initializing its children.
	 */
	public final void refresh() { if(!isOpen()) return; clearChildren(); init(); }
	// --------------------------------------------------
	protected final @Override void init()
	{
		//initialize the content pane panel
		this.bsPanel = new BetterStatsPanel(0, 0, getWidth(), getHeight(), new BetterStatsPanelProxy()
		{
			public IStatsProvider getStatsProvider() { return BetterStatsScreen.this.statsProvider; }
			public StatsTab getSelectedStatsTab() { return BetterStatsScreen.this.selectedStatsTab; }
			public void setSelectedStatsTab(StatsTab statsTab) { setStatsTab(statsTab); }
			public final @Override StatFilterSettings getFilterSettings() { return BetterStatsScreen.this.filterSettings; }
		});
		addChild(this.bsPanel, false);
	}
	// --------------------------------------------------
	public final @Override boolean filesDragged(Collection<Path> files)
	{
		//make sure only one file is being dragged
		if(files.size() != 1) return false;
		
		//obtain dragged file, and verify its extension
		final Path file = files.iterator().next();
		if(!file.toString().endsWith("." + StatsProviderIO.FILE_EXTENSION.toLowerCase()))
			return false;
		
		//attempt to load the file
		try
		{
			final var stats = StatsProviderIO.loadFromFile(file.toFile());
			final var screen = new BetterStatsScreen(this.parent, stats).getAsScreen();
			BetterStatsClient.MC_CLIENT.setScreen(screen);
		}
		catch(IOException exc) { return false; }
		
		//indicate success
		return true;
	}
	// ==================================================
}