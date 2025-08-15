package io.github.thecsdev.betterstats.api.client.gui.widget;

import java.awt.Color;

import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TScrollBarWidget;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;

/**
 * A {@link TScrollBarWidget} implementation whose visuals use
 * flat/static colors, instead of the default textures.
 */
public @Virtual class ScrollBarWidget extends TScrollBarWidget
{
	// ==================================================
	private static final int COLOR_BLACK = Color.BLACK.getRGB();
	private static final int COLOR_NORMAL = new Color(255,255,255,50).getRGB();
	private static final int COLOR_HOVERED = new Color(255,255,255,110).getRGB();
	// ==================================================
	public ScrollBarWidget(int x, int y, int width, int height, TPanelElement target) { super(x, y, width, height, target); }
	public ScrollBarWidget(int x, int y, int width, int height, TPanelElement target, boolean autoSetScrollFlags) { super(x, y, width, height, target, autoSetScrollFlags); }
	// ==================================================
	public @Virtual @Override void render(TDrawContext pencil)
	{
		pencil.drawTFill(1342177280);
		pencil.drawTBorder(COLOR_BLACK);
		renderSliderKnob(pencil);
	}
	// --------------------------------------------------
	public @Virtual @Override void renderSliderProgressBar(TDrawContext pencil) {/*doesn't have a visual one*/}
	// --------------------------------------------------
	public @Virtual @Override void renderSliderKnob
	(TDrawContext pencil, int knobX, int knobY, int knobWidth, int knobHeight)
	{
		pencil.fill(
				knobX, knobY,
				knobX + knobWidth, knobY + knobHeight,
				isFocusedOrHovered() ? COLOR_HOVERED : COLOR_NORMAL);
	}
	// ==================================================
}