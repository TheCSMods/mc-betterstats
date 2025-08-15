package io.github.thecsdev.betterstats.client.gui.widget;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.SIZE;
import static io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement.COLOR_OUTLINE_FOCUSED;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TClickableWidget;

/**
 * A stat widget representing statistics about an {@link Advancement}.
 */
@Experimental
public final @Internal class AdvancementStatWidget extends TClickableWidget
{
	// ==================================================
	private final AdvancementNode advancement;
	private final ItemStack   displayItem;
	// --------------------------------------------------
	private int backgroundColor = 0;
	private @Nullable Consumer<AdvancementStatWidget> onClick;
	// ==================================================
	public AdvancementStatWidget(int x, int y, AdvancementNode advancement) throws NullPointerException
	{
		super(x, y, SIZE, SIZE);
		this.advancement = Objects.requireNonNull(advancement);
		
		final var tooltip = literal("");
		final @Nullable var display = advancement.advancement().display().orElse(null);
		if(display != null)
		{
			tooltip.append(literal("").append(display.getTitle()).withStyle(ChatFormatting.YELLOW));
			tooltip.append("\n");
			tooltip.append(literal("").append(display.getDescription()).withStyle(ChatFormatting.GRAY));
			this.displayItem = display.getIcon();
			
			this.backgroundColor = switch(display.getType())
			{
				case TASK      -> TPanelElement.COLOR_BACKGROUND;
				case GOAL      -> 0x50223333;
				case CHALLENGE -> 0x44330066;
				default        -> TPanelElement.COLOR_BACKGROUND;
			};
		}
		else
		{
			tooltip.append(literal(advancement.holder().id().toString()).withStyle(ChatFormatting.YELLOW));
			this.displayItem = Items.AIR.getDefaultInstance();
			this.backgroundColor = TPanelElement.COLOR_BACKGROUND;
		}
		setTooltip(Tooltip.create(tooltip));
	}
	// ==================================================
	/**
	 * Retrieves the "on-click" action of this {@link AdvancementStatWidget}.
	 */
	public final @Nullable Consumer<AdvancementStatWidget> getOnClick() { return this.onClick; }
	
	/**
	 * Sets the action that will take place when this {@link AdvancementStatWidget} is clicked.
	 */
	public final void setOnClick(@Nullable Consumer<AdvancementStatWidget> onClick) { this.onClick = onClick; }
	// --------------------------------------------------
	/**
	 * Returns the associated {@link Advancement}.
	 */
	public final AdvancementNode getAdvancement() { return this.advancement; }
	// ==================================================
	protected final @Override void onClick() { if(this.onClick != null) this.onClick.accept(this); }
	// --------------------------------------------------
	public final @Override void render(TDrawContext pencil)
	{
		//draw the solid background color, and then the display item
		pencil.drawTFill(this.backgroundColor);
		pencil.renderItem(this.displayItem, getX() + 3, getY() + 3);
	}
	
	public final @Override void postRender(TDrawContext pencil)
	{
		//draw an outline when the widget is hovered or focused
		if(isFocusedOrHovered())
			pencil.drawTBorder(COLOR_OUTLINE_FOCUSED);
	}
	// ==================================================
}