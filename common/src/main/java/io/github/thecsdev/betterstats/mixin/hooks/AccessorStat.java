package io.github.thecsdev.betterstats.mixin.hooks;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Stat.class)
public interface AccessorStat
{
	@Accessor("formatter") StatFormatter getFormatter();
}