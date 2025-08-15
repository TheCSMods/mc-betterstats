package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IEditableStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.client.network.OtherClientPlayerStatsProvider;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;

public final class StatsHudItemEntry extends TWidgetHudScreen.WidgetEntry<TElement>
{
	// ==================================================
	static final int WIDTH = 100;
	// --------------------------------------------------
	protected IStatsProvider statsProvider;
	protected final Item item;
	protected final @Nullable Block block;
	protected StatType<?> mode = Stats.ITEM_USED;
	// ==================================================
	public StatsHudItemEntry(SUItemStat stat) throws NullPointerException { this(stat.getStatsProvider(), stat.getItem()); }
	public StatsHudItemEntry(IStatsProvider statsProvider, Item item) throws NullPointerException
	{
		super(0.5, 0.25);
		this.statsProvider = Objects.requireNonNull(statsProvider);
		this.item = Objects.requireNonNull(item);
		final var block = Block.byItem(item);
		this.block = (block != Blocks.AIR) ? block : null;
	}
	// ==================================================
	public final @Override TElement createWidget()
	{
		//ensure local stat providers are up-to-date
		if(this.statsProvider instanceof LocalPlayerStatsProvider)
			this.statsProvider = Objects.requireNonNull(LocalPlayerStatsProvider.getInstance());
		
		//create and return element
		final var el = new Element();
		el.eContextMenu.register((__, cMenu) ->
		{
			//item and block stat type entries
			for(final var statType : BuiltInRegistries.STAT_TYPE)
			{
				//check the stat type and if it's compatible
				final var isItem = (statType.getRegistry() == BuiltInRegistries.ITEM);
				final var isBlock = (statType.getRegistry() == BuiltInRegistries.BLOCK);
				if((!isItem && !isBlock) || (isBlock && this.block == null)) continue;
				
				//create a button that will switch to the given stat type, and refresh
				cMenu.addButton(statType.getDisplayName(), ___ ->
				{
					this.mode = statType;
					refreshEntry();
				});
			}
			
			//remove entry button
			cMenu.addSeparator();
			cMenu.addButton(translatable("selectWorld.delete"), ___ -> removeEntry());
		});
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
			//super
			super(0, 0, WIDTH, CustomStatElement.HEIGHT);
			
			//item stat widget
			final var stat = new SUItemStat(StatsHudItemEntry.this.statsProvider, StatsHudItemEntry.this.item);
			addChild(new ItemStatWidget(0, 0, stat), true);
			
			//custom stat element
			{
				//mode info
				if(StatsHudItemEntry.this.mode == null) StatsHudItemEntry.this.mode = Stats.ITEM_USED;
				final var isItem = (StatsHudItemEntry.this.mode.getRegistry() == BuiltInRegistries.ITEM);
				final var isBlock = (StatsHudItemEntry.this.mode.getRegistry() == BuiltInRegistries.BLOCK);
				
				//collect info
				final Object valObj = (isBlock) ? StatsHudItemEntry.this.block : (isItem ? StatsHudItemEntry.this.item : null);
				@SuppressWarnings("unchecked")
				final var val = (valObj != null) ?
					stat.getStatsProvider().getStatValue((StatType<Object>)StatsHudItemEntry.this.mode, valObj) :
					-1; //-1 indicating an error, and this generally shouldn't happen
				
				//create texts
				final Component left = Optional.ofNullable(StatsHudItemEntry.this.mode.getDisplayName())
						.orElse(literal(Objects.toString(BuiltInRegistries.STAT_TYPE.getKey(mode))));
				final Component right = literal(Integer.toString(val));
				
				//update width
				this.setSize(this.width + getTextRenderer().width(left), this.height);
				
				//create custom stat element
				final var cse = new CustomStatElement(ItemStatWidget.SIZE, 0, this.width - ItemStatWidget.SIZE, left, right);
				addChild(cse, true);
			}
		}
		public @Override void render(TDrawContext pencil)
		{
			pencil.drawTFill(TPanelElement.COLOR_BACKGROUND);
			pencil.drawTFill(this.overlayColor);
		}
	}
	// ==================================================
}