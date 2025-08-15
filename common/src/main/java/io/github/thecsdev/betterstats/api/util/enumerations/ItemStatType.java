package io.github.thecsdev.betterstats.api.util.enumerations;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;

public enum ItemStatType implements ITextProvider
{
	// ==================================================
	MINED(Stats.BLOCK_MINED, translatable("stat_type.minecraft.mined"), s -> s.mined),
	CRAFTED(Stats.ITEM_CRAFTED, translatable("stat_type.minecraft.crafted"), s -> s.crafted),
	PICKED_UP(Stats.ITEM_PICKED_UP, translatable("stat_type.minecraft.picked_up"), s -> s.pickedUp),
	DROPPED(Stats.ITEM_DROPPED, translatable("stat_type.minecraft.dropped"), s -> s.dropped),
	USED(Stats.ITEM_USED, translatable("stat_type.minecraft.used"), s -> s.used),
	BROKEN(Stats.ITEM_BROKEN, translatable("stat_type.minecraft.broken"), s -> s.broken);
	// ==================================================
	private final StatType<?> statType;
	private final Component text;
	private final Function<SUItemStat, Integer> statValueSupplier;
	// ==================================================
	private ItemStatType(StatType<?> statType, Component text, Function<SUItemStat, Integer> statValueSupplier)
	{
		this.statType = Objects.requireNonNull(statType);
		this.text = Objects.requireNonNull(text);
		this.statValueSupplier = Objects.requireNonNull(statValueSupplier);
	}
	// ==================================================
	public final StatType<?> getStatType() { return this.statType; }
	public final @Override Component getText() { return this.text; }
	public final int getStatValue(SUItemStat stat) throws NullPointerException
	{
		return this.statValueSupplier.apply(Objects.requireNonNull(stat));
	}
	// --------------------------------------------------
	public static final boolean isItemStat(Stat<?> stat)
	{
		final var statType = stat.getType();
		for(final var val : ItemStatType.values())
			if(Objects.equals(statType, val.statType))
				return true;
		return false;
	}
	// ==================================================
}