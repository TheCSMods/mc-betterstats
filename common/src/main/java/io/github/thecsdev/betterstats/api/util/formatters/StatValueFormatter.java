package io.github.thecsdev.betterstats.api.util.formatters;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.api.registry.BSRegistries.STAT_DISTANCE_FORMATTER;
import static io.github.thecsdev.betterstats.api.registry.BSRegistries.STAT_TIME_FORMATTER;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;

/**
 * Similar to {@link StatFormatter}, but {@link BetterStats}'s way of handling it.
 * @since 3.13
 */
@Experimental
public abstract class StatValueFormatter
{
	// ==================================================
	public static final StatValueFormatter TIME;
	public static final StatValueFormatter TIME_TICKS;
	public static final StatValueFormatter TIME_MILLISECONDS;
	public static final StatValueFormatter TIME_SECONDS;
	public static final StatValueFormatter TIME_MINUTES;
	public static final StatValueFormatter TIME_HOURS;
	public static final StatValueFormatter TIME_DAYS;
	public static final StatValueFormatter TIME_WEEKS;
	public static final StatValueFormatter TIME_YEARS;
	public static final StatValueFormatter TIME_HH_MM_SS_MS;
	// --------------------------------------------------
	public static final StatValueFormatter DISTANCE;
	public static final StatValueFormatter DISTANCE_CENTIMETER;
	public static final StatValueFormatter DISTANCE_METER;
	public static final StatValueFormatter DISTANCE_KILOMETER;
	public static final StatValueFormatter DISTANCE_INCH;
	public static final StatValueFormatter DISTANCE_FOOT;
	public static final StatValueFormatter DISTANCE_YARD;
	public static final StatValueFormatter DISTANCE_MILE;
	// ==================================================
	static
	{
		/*
		 * Define formatters for time.
		 */
		TIME = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("-"); }
			public final @Override Component format(int ticks) { return literal(StatFormatter.TIME.format(ticks)); }
		};
		TIME_TICKS = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("20/s"); }
			public final @Override Component format(int ticks) { return literal(Integer.toString(ticks)); }
		};
		TIME_MILLISECONDS = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("ms"); }
			public final @Override Component format(int ticks) { return calc(ticks, new BigDecimal(0.02)).append(" ms"); }
		};
		TIME_SECONDS = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("s"); }
			public final @Override Component format(int ticks) { return calc(ticks, new BigDecimal(20)).append(" s"); }
		};
		TIME_MINUTES = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("m"); }
			public final @Override Component format(int ticks) { return calc(ticks, new BigDecimal(1200)).append(" m"); }
		};
		TIME_HOURS = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("h"); }
			public final @Override Component format(int ticks) { return calc(ticks, new BigDecimal(72000)).append(" h"); }
		};
		TIME_DAYS = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("d"); }
			public final @Override Component format(int ticks) { return calc(ticks, new BigDecimal(1728000)).append(" d"); }
		};
		TIME_WEEKS = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("w"); }
			public final @Override Component format(int ticks) { return calc(ticks, new BigDecimal(12096000)).append(" w"); }
		};
		TIME_YEARS = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("y"); }
			public final @Override Component format(int ticks) { return calc(ticks, new BigDecimal(631152000)).append(" y"); }
		};
		TIME_HH_MM_SS_MS = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("hh:mm:ss.ms"); }
			public final @Override Component format(int ticks)
			{
				// Constants for calculations
				final int TICKS_PER_SECOND   = 20;
				final int SECONDS_PER_MINUTE = 60;
				final int MINUTES_PER_HOUR   = 60;
				
				// Converting ticks to total seconds
				final int totalSeconds = ticks / TICKS_PER_SECOND;
				
				// Calculating hours, minutes, and seconds
				final int hours   = totalSeconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR);
				final int minutes = (totalSeconds % (SECONDS_PER_MINUTE * MINUTES_PER_HOUR)) / SECONDS_PER_MINUTE;
				final int seconds = totalSeconds % SECONDS_PER_MINUTE;

				// Calculating milliseconds
				final int milliseconds = (ticks % TICKS_PER_SECOND) * 50; // 1000 ms in a second / 20 ticks

				// Formatting result to "hh:mm:ss.ms"
				final var result = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
				return literal(result);
			}
		};
		
		/*
		 * Define formatters for distance.
		 */
		DISTANCE = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("-"); }
			public final @Override Component format(int cm) { return literal(StatFormatter.DISTANCE.format(cm)); }
		};
		DISTANCE_CENTIMETER = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("cm"); }
			public final @Override Component format(int cm) { return literal(Integer.toString(cm)).append(" cm"); }
		};
		DISTANCE_METER = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("m"); }
			public final @Override Component format(int cm) { return calc(cm, new BigDecimal(100)).append(" m"); }
		};
		DISTANCE_KILOMETER = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("km"); }
			public final @Override Component format(int cm) { return calc(cm, new BigDecimal(100000)).append(" km"); }
		};
		DISTANCE_INCH = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("in"); }
			public final @Override Component format(int cm) { return calc(cm, new BigDecimal(2.54)).append(" in"); }
		};
		DISTANCE_FOOT = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("ft"); }
			public final @Override Component format(int cm) { return calc(cm, new BigDecimal(30.48)).append(" ft"); }
		};
		DISTANCE_YARD = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("yd"); }
			public final @Override Component format(int cm) { return calc(cm, new BigDecimal(91.44)).append(" yd"); }
		};
		DISTANCE_MILE = new StatValueFormatter()
		{
			public final @Override Component getDisplayName() { return literal("mi"); }
			public final @Override Component format(int cm) { return calc(cm, new BigDecimal(160934.4)).append(" mi"); }
		};
		
		/*
		 * Register the formatters for time and distance
		 */
		final var modId = getModID();
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "default"),     TIME);
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "ms"),          TIME_MILLISECONDS);
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "s"),           TIME_SECONDS);
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "m"),           TIME_MINUTES);
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "h"),           TIME_HOURS);
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "d"),           TIME_DAYS);
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "w"),           TIME_WEEKS);
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "y"),           TIME_YEARS);
		STAT_TIME_FORMATTER    .register(ResourceLocation.fromNamespaceAndPath(modId, "hh_mm_ss_ms"), TIME_HH_MM_SS_MS);
		STAT_DISTANCE_FORMATTER.register(ResourceLocation.fromNamespaceAndPath(modId, "default"),     DISTANCE);
		STAT_DISTANCE_FORMATTER.register(ResourceLocation.fromNamespaceAndPath(modId, "cm"),          DISTANCE_CENTIMETER);
		STAT_DISTANCE_FORMATTER.register(ResourceLocation.fromNamespaceAndPath(modId, "m"),           DISTANCE_METER);
		STAT_DISTANCE_FORMATTER.register(ResourceLocation.fromNamespaceAndPath(modId, "km"),          DISTANCE_KILOMETER);
		STAT_DISTANCE_FORMATTER.register(ResourceLocation.fromNamespaceAndPath(modId, "in"),          DISTANCE_INCH);
		STAT_DISTANCE_FORMATTER.register(ResourceLocation.fromNamespaceAndPath(modId, "ft"),          DISTANCE_FOOT);
		STAT_DISTANCE_FORMATTER.register(ResourceLocation.fromNamespaceAndPath(modId, "yd"),          DISTANCE_YARD);
		STAT_DISTANCE_FORMATTER.register(ResourceLocation.fromNamespaceAndPath(modId, "mi"),          DISTANCE_MILE);
	}
	// ==================================================
	/**
	 * Returns the display name of the formatter.
	 * @apiNote Examples include: "Seconds", "Minutes", "Hours", "Meters", "Kilometers", "Megameters", and so on...
	 */
	public abstract Component getDisplayName();
	// --------------------------------------------------
	/**
	 * Formats a number.
	 * @param number The number to format.
	 */
	public abstract Component format(int number);
	// ==================================================
	/**
	 * Formats a {@link String} that represents a decimal number.
	 */
	private static final @Internal String fdn(String str)
	{
		str = str.length() > 7 ? str.substring(0, 7) : str;
		str = str.replaceAll("(\\.)?0*$", "");
		return str;
	}
	
	/**
	 * Calculates a measurement unit and returns the result as formatted {@link Component}.
	 */
	private static final MutableComponent calc(int input, BigDecimal divisor)
	{
		final var num = new BigDecimal(input).divide(divisor, 5, RoundingMode.DOWN);
		return literal(fdn(num.toString()));
	}
	// ==================================================
}