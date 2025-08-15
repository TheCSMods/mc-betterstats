package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.client.gui.widget.AdvancementStatWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UIAutomaticSizeLayout;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UIHorizontalGridLayout;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TBlankElement;
import io.github.thecsdev.tcdcommons.api.util.enumerations.AutomaticSize;

/**
 * A {@link StatsTab} that displays information about advancements.
 */
@Experimental
public final @Internal class AdvancementsTab extends StatsTab
{
	// ==================================================
	public final @Override Component getName() { return translatable("gui.advancements"); }
	// ==================================================
	public final @Override boolean isAvailable() { return false; }
	// --------------------------------------------------
	public final @Override void initStats(StatsInitContext initContext)
	{
		//prepare
		final var panel = initContext.getStatsPanel();
		final @Nullable var srv = MC_CLIENT.getConnection();
		if(srv == null) return;
		
		//collect the advancement categories, aka "roots"
		final var aRoots = srv.getAdvancements().getTree().nodes().stream()
				.collect(Collectors.groupingBy(a -> a.root()));
		aRoots.remove(null); //just in case
		
		//iterate all groups, and list their stats
		for(final var aRoot : aRoots.keySet())
		{
			//initialize the group label
			final var arDisplay = aRoot.advancement().display().orElse(null);
			if(arDisplay == null) continue;
			StatsTabUtils.initGroupLabel(panel, arDisplay.getTitle());
			
			//prepare to initialize the group stats
			final var n1 = UILayout.nextChildVerticalRect(panel);
			final var div = new TBlankElement(n1.x, n1.y, n1.width, 0);
			panel.addChild(div, false);
			
			//initialize group advancement stats
			for(final var advancement : aRoots.get(aRoot))
			{
				//skip advancements that list themselves as roots
				if(advancement == aRoot) continue;
				
				//add advancement stat widget
				final var widget = new AdvancementStatWidget(0, 0, advancement);
				div.addChild(widget, false);
			}
			
			//apply UI layouts
			new UIHorizontalGridLayout().apply(div);
			new UIAutomaticSizeLayout(AutomaticSize.Y).apply(div);
		}
	}
	// ==================================================
}