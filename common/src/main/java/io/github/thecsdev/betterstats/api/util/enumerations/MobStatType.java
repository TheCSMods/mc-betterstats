package io.github.thecsdev.betterstats.api.util.enumerations;

import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;

public enum MobStatType implements ITextProvider
{
	// ==================================================
	KILLED(Stats.ENTITY_KILLED, BST.stp_mc_killed(), s -> s.kills),
	KILLED_BY(Stats.ENTITY_KILLED_BY, BST.stp_mc_killedBy(), s -> s.deaths);
	// ==================================================
	private final StatType<?> statType;
	private final Component text;
	private final Function<SUMobStat, Integer> statValueSupplier;
	// ==================================================
	private MobStatType(StatType<?> statType, Component text, Function<SUMobStat, Integer> statValueSupplier)
	{
		this.statType = Objects.requireNonNull(statType);
		this.text = Objects.requireNonNull(text);
		this.statValueSupplier = Objects.requireNonNull(statValueSupplier);
	}
	// ==================================================
	public final StatType<?> getStatType() { return this.statType; }
	public final @Override Component getText() { return this.text; }
	public final int getStatValue(SUMobStat stat) throws NullPointerException
	{
		return this.statValueSupplier.apply(Objects.requireNonNull(stat));
	}
	// --------------------------------------------------
	public static final boolean isMobStat(Stat<?> stat)
	{
		final var statType = stat.getType();
		for(final var val : MobStatType.values())
			if(Objects.equals(statType, val.statType))
				return true;
		return false;
	}
	// ==================================================
}