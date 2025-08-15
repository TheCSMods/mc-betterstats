package io.github.thecsdev.betterstats.api.client.util;

import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel;
import io.github.thecsdev.tcdcommons.api.util.collections.GenericProperties;
import net.minecraft.resources.ResourceLocation;

/**
 * An instance of {@link GenericProperties} whose sole purpose is to store
 * information about "statistics filters" applied to a {@link BetterStatsPanel}.
 */
public final class StatFilterSettings extends GenericProperties<ResourceLocation>
{
	private static final long serialVersionUID = 4330232763610691948L;
}