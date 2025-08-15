package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * An alternative to {@link MobStatWidget}, displaying an {@link SUMobStat}
 * in a {@link GeneralStatWidget}-like fashion.
 */
public @Virtual class MobStatTextWidget extends AbstractStatWidget<SUMobStat>
{
	// ==================================================
	protected final Component txt_label;
	protected final Component txt_value;
	// ==================================================
	@SuppressWarnings("deprecation")
	public MobStatTextWidget(int x, int y, int width, SUMobStat stat) throws NullPointerException
	{
		super(x, y, width, GeneralStatWidget.HEIGHT, stat);
		this.txt_label = stat.getStatLabel();
		this.txt_value = Component.literal("⚔" + stat.kills + " / 💀" + stat.deaths);
		
		setTooltip(Tooltip.create(MobStatWidget.createTooltipText(stat)));
	}
	// ==================================================
	public @Virtual @Override void render(TDrawContext pencil)
	{
		super.render(pencil);
		pencil.drawTElementTextTH(this.txt_label, HorizontalAlignment.LEFT);
		pencil.drawTElementTextTH(this.txt_value, HorizontalAlignment.RIGHT);
	}
	// ==================================================
}