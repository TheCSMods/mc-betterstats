package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.tcdcommons.api.client.registry.TClientRegistries.PLAYER_BADGE_RENDERER;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.util.stats.SUPlayerBadgeStat;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadge;
import io.github.thecsdev.tcdcommons.api.client.badge.ClientPlayerBadge;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.render.badge.PlayerBadgeRenderer;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;

@Deprecated(since = "3.13.9")
public @Virtual class PlayerBadgeStatWidget extends AbstractStatWidget<SUPlayerBadgeStat>
{
	// ==================================================
	public static final int SIZE = 25;
	//
	public static final Component TXT_STAT_OBTAINED = BST.sWidget_pbadge_obtained();
	// --------------------------------------------------
	protected final PlayerBadge playerBadge;
	protected final @Nullable PlayerBadgeRenderer<?> playerBadgeRenderer;
	//
	protected final Tooltip defaultTooltip;
	// ==================================================
	public PlayerBadgeStatWidget(int x, int y, SUPlayerBadgeStat stat) throws NullPointerException
	{
		super(x, y, SIZE, SIZE, stat);
		this.playerBadge = Objects.requireNonNull(stat.getPlayerBadge());
		this.playerBadgeRenderer = PLAYER_BADGE_RENDERER.getValue(stat.getStatID()).orElse(null);
		
		final Component ttt = literal("") //MUST create new text instance
				.append(stat.getStatLabel())
				.append(fLiteral("\n§7" + stat.getStatID()))
				.append("\n\n§r")
				.append(this.playerBadge.getDescription())
				.append("\n\n")
				.append(fLiteral("§e" + TXT_STAT_OBTAINED.getString() + ": §r" + stat.value))
				.append((this.playerBadge instanceof ClientPlayerBadge) ?
						fLiteral("\n\n§9" + translatable("tcdcommons.client_side").getString()) :
						literal(""));
		setTooltip(this.defaultTooltip = Tooltip.create(ttt));
	}
	// ==================================================
	public @Virtual @Override void render(TDrawContext pencil)
	{
		super.render(pencil);
		if(this.playerBadgeRenderer != null)
			this.playerBadgeRenderer.render(
					pencil,
					this.getX() + 2, this.getY() + 2,
					this.getWidth() - 4, this.getHeight() - 4,
					pencil.mouseX, pencil.mouseY,
					pencil.deltaTime
				);
		else pencil.drawTFill(TDrawContext.DEFAULT_ERROR_COLOR);
	}
	// ==================================================
}