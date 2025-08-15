package io.github.thecsdev.betterstats.api.client.gui.panel;

import static io.github.thecsdev.betterstats.BetterStats.getModID;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TRefreshablePanelElement;
import net.minecraft.resources.ResourceLocation;

/**
 * A {@link TRefreshablePanelElement} representing a {@link BetterStatsScreen} GUI component.
 * @apiNote Not intended for outside use. Please use {@link TRefreshablePanelElement} instead.
 */
public abstract class BSComponentPanel extends TRefreshablePanelElement
{
	// ==================================================
	/**
	 * {@link BetterStats}'s "{@code textures/gui/widgets.png}" texture {@link ResourceLocation}.
	 */
	public static final ResourceLocation BS_WIDGETS_TEXTURE = ResourceLocation.fromNamespaceAndPath(getModID(), "textures/gui/widgets.png");
	// ==================================================
	public BSComponentPanel(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		this.scrollFlags = 0;
		this.scrollPadding = 0;
		this.outlineColor = -16777216; //black
	}
	// ==================================================
}