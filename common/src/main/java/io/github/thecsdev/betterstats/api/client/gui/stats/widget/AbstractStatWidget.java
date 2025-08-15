package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;

import java.util.Objects;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import io.github.thecsdev.betterstats.api.util.stats.SUStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TClickableWidget;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;

/**
 * A GUI widget that displays stats information from a given {@link SUStat}.
 */
public abstract class AbstractStatWidget<S extends SUStat<?>> extends TClickableWidget
{
	// ==================================================
	/**
	 * The {@link SUStat}.
	 */
	protected final S stat;
	// --------------------------------------------------
	/**
	 * The background fill color of the {@link AbstractStatWidget}.
	 */
	protected int backgroundColor = TPanelElement.COLOR_BACKGROUND;
	
	/**
	 * The outline color used when this {@link AbstractStatWidget} is <b>not</b> focused or hovered.
	 */
	protected int outlineColor = 0;
	
	/**
	 * The outline color used when this {@link AbstractStatWidget} is focused or hovered.
	 */
	protected int focusOutlineColor = TPanelElement.COLOR_OUTLINE_FOCUSED;
	// ==================================================
	public AbstractStatWidget(int x, int y, int width, int height, S stat) throws NullPointerException
	{
		super(x, y, width, height);
		this.stat = Objects.requireNonNull(stat);
	}
	// ==================================================
	/**
	 * Returns the {@link SUStat} associated with this {@link AbstractStatWidget}.
	 */
	public final S getStat() { return this.stat; }
	// --------------------------------------------------
	public final int getBackgroundColor() { return this.backgroundColor; }
	public final int getOutlineColor() { return this.outlineColor; }
	public final int getFocusOutlineColor() { return this.focusOutlineColor; }
	public @Virtual void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }
	public @Virtual void setOutlineColor(int outlineColor) { this.outlineColor = outlineColor; }
	public @Virtual void setFocusOutlineColor(int focusOutlineColor) { this.focusOutlineColor = focusOutlineColor; }
	// ==================================================
	protected @Virtual @Override void onClick() {}
	// --------------------------------------------------
	public @Virtual @Override boolean input(TInputContext inputContext)
	{
		//handle input based on type
		switch(inputContext.getInputType())
		{
			case MOUSE_PRESS: //clearing focus when clicking and focused
				if(isFocused()) { getParentTScreen().setFocusedElement(null); return true; }
				break;
			default: break;
		}
		//return super by default
		return super.input(inputContext);
	}
	// --------------------------------------------------
	public @Virtual @Override void render(TDrawContext pencil) { pencil.drawTFill(this.backgroundColor); }
	public @Virtual @Override void postRender(TDrawContext pencil)
	{
		//render the borders
		if(isFocusedOrHovered()) pencil.drawTBorder(this.focusOutlineColor);
		else pencil.drawTBorder(this.outlineColor);
		
		//a neat little on-hover sound
		if(isHovered() && !isFocused() && !this.__wasHovered)
			MC_CLIENT.getSoundManager().play(SimpleSoundInstance.forUI(
					SoundEvents.NOTE_BLOCK_HAT.value(), 1.8f, 0.05f));
		this.__wasHovered = isHovered();
	}
	private boolean __wasHovered = false;
	// ==================================================
}