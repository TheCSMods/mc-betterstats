package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.betterstats.api.registry.BSRegistries.getEntityStatTypePhrase;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.registry.BSRegistries;
import io.github.thecsdev.betterstats.api.util.enumerations.MobStatType;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TEntityRendererElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext.InputType;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.stats.StatType;
import net.minecraft.world.entity.EntityType;

@Deprecated(since = "3.13.9")
public @Virtual class MobStatWidget extends AbstractStatWidget<SUMobStat>
{
	// ==================================================
	private static final BetterStatsConfig BSS_CONFIG = BetterStats.getInstance().getConfig(); //optimization
	// --------------------------------------------------
	public static final int SIZE = 55;
	//
	public static final Component TEXT_STAT_KILLS  = MobStatType.KILLED.getText();
	public static final Component TEXT_STAT_DEATHS = MobStatType.KILLED_BY.getText();
	// --------------------------------------------------
	protected final EntityType<?> entityType;
	protected final TEntityRendererElement entityRenderer;
	//
	protected final Tooltip defaultTooltip;
	// ==================================================
	public MobStatWidget(int x, int y, SUMobStat stat) throws NullPointerException { this(x, y, SIZE, stat); }
	public MobStatWidget(int x, int y, int size, SUMobStat stat) throws NullPointerException
	{
		super(x, y, size, size, stat);
		this.entityType = stat.getEntityType();
		
		setTooltip(this.defaultTooltip = Tooltip.create(createTooltipText(stat)));
		
		this.entityRenderer = new TEntityRendererElement(x, y, size, size, this.entityType);
		this.entityRenderer.setFollowsCursor(BSS_CONFIG.guiMobsFollowCursor);
		addChild(this.entityRenderer, false);
	}
	// --------------------------------------------------
	public @Virtual @Override void setSize(int width, int height, int flags)
	{
		super.setSize(width, height, flags);
		this.entityRenderer.setSize(width, height, flags);
	}
	// ==================================================
	public @Virtual @Override boolean input(TInputContext inputContext)
	{
		//only handle mouse presses
		if(inputContext.getInputType() != InputType.MOUSE_PRESS)
			return false;
		
		//handle the mouse press
		final int btn = inputContext.getMouseButton();
		
		//handle input
		if(btn == 2)
		{
			final @Nullable var url = BSRegistries.getMobWikiURL(this.stat.getStatID());
			if(url != null)
			{
				GuiUtils.showUrlPrompt(url, false);
				return false; //if successful, block the focus by returning false
			}
		}
		
		//return super
		return super.input(inputContext);
	}
	// ==================================================
	/**
	 * Creates a {@link Component} instance for a tooltip belonging to a mob stat element.
	 * @param stat The {@link SUMobStat}.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final Component createTooltipText(SUMobStat stat) throws NullPointerException
	{
		//prepare the tooltip text
		final MutableComponent ttt = literal("") //MUST create new text instance
				.append(literal("").append(stat.getStatLabel()).withStyle(ChatFormatting.YELLOW))
				.append("\n§7" + stat.getStatID())
				.append("\n§r");
		
		//iterate all registered stat types, to append their values to the tooltip text
		for(final var statType : BuiltInRegistries.STAT_TYPE)
		{
			//ignore all registry types except ENTITY_TYPE; also ignore the two vanilla stat types
			if(statType.getRegistry() != BuiltInRegistries.ENTITY_TYPE) continue;
			
			//obtain the text formatter for this stat type
			final @Nullable var textFormatter = BSRegistries.ENTITY_STAT_TEXT_FORMATTER.get(statType);
			if(textFormatter != null) ttt.append("\n§e-§r ").append(textFormatter.apply(stat));
			else
			{
				@SuppressWarnings("unchecked")
				final var statTypeE = (StatType<EntityType<?>>)statType;
				final int stVal = stat.getStatsProvider().getStatValue(statTypeE, stat.getEntityType());
				ttt.append("\n§e-§r ")
					.append(getEntityStatTypePhrase(statTypeE))
					.append(": " + statTypeE.get(stat.getEntityType()).format(stVal));
			}
		}
		
		//return the result
		return ttt;
	}
	// ==================================================
}