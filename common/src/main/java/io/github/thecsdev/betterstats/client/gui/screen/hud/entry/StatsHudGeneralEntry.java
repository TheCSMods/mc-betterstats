package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import org.jetbrains.annotations.ApiStatus.Experimental;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.formatters.StatValueFormatter;
import io.github.thecsdev.betterstats.api.util.io.IEditableStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.betterstats.client.network.OtherClientPlayerStatsProvider;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;

public class StatsHudGeneralEntry extends TWidgetHudScreen.WidgetEntry<TElement>
{
	// ==================================================
	static final int WIDTH = 100;
	// --------------------------------------------------
	protected IStatsProvider statsProvider;
	protected final ResourceLocation generalStat;
	protected StatType<ResourceLocation> mode = Stats.CUSTOM;
	//
	protected final @Experimental StatValueFormatter formatter; //since v3.13
	// ==================================================
	public StatsHudGeneralEntry(SUGeneralStat stat, StatValueFormatter formatter) throws NullPointerException {
		this(stat.getStatsProvider(), stat.getGeneralStat().getValue(), formatter);
	}
	public StatsHudGeneralEntry(
			IStatsProvider statsProvider,
			ResourceLocation generalStat,
			StatValueFormatter formatter) throws NullPointerException
	{
		super(0.5, 0.25);
		this.statsProvider = Objects.requireNonNull(statsProvider);
		this.generalStat = Objects.requireNonNull(generalStat);
		this.formatter = Objects.requireNonNull(formatter);
	}
	// ==================================================
	public final @Override TElement createWidget()
	{
		//ensure local stat providers are up-to-date
		if(this.statsProvider instanceof LocalPlayerStatsProvider)
			this.statsProvider = Objects.requireNonNull(LocalPlayerStatsProvider.getInstance());
		
		//create the element, and add the context menu to it
		if(this.mode == null) this.mode = Stats.CUSTOM;
		final var el = new Element();
		el.eContextMenu.register((__, cMenu) ->
		{
			cMenu.addButton(translatable("selectWorld.delete"), ___ -> removeEntry());
		});
		
		//return the new element
		return el;
	}
	// ==================================================
	private final class Element extends TElement
	{
		private int overlayColor = (statsProvider instanceof IEditableStatsProvider) ?
				((statsProvider instanceof OtherClientPlayerStatsProvider) ? 0x7700aaff : 0x55ff0000) :
				0;
		public Element()
		{
			//init super
			super(0, 0, WIDTH, CustomStatElement.HEIGHT);
			
			//obtain values and texts
			int rightVar = statsProvider.getStatValue(mode, generalStat);
			var left = (mode == Stats.CUSTOM) ?
					SUGeneralStat.getGeneralStatText(mode.get(generalStat)) :
					literal("<Unsupported stat>");
			var right = StatsHudGeneralEntry.this.formatter.format(rightVar);
			
			//update width
			this.setSize(this.width + getTextRenderer().width(left), this.height);
			
			//add custom stat element child
			addChild(new CustomStatElement(0, 0, this.width, left, right));
		}
		public @Override void render(TDrawContext pencil)
		{
			pencil.drawTFill(TPanelElement.COLOR_BACKGROUND);
			pencil.drawTFill(this.overlayColor);
		}
	}
	// ==================================================
}