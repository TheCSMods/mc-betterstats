package io.github.thecsdev.betterstats.command;

import static io.github.thecsdev.tcdcommons.command.PlayerBadgeCommand.handleError;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import io.github.thecsdev.tcdcommons.mixin.hooks.AccessorStatHandler;

public final class StatisticsCommand
{
	// ==================================================
	public static final Component TEXT_CLEAR_KICK = BST.cmd_stats_clear_kick();
	// ==================================================
	private StatisticsCommand() {}
	// ==================================================
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext cra)
	{
		//define the command and its alt.
		final var statistics = literal("statistics").requires(scs -> scs.hasPermission(2))
				.then(statistics_edit(cra))
				.then(statistics_clear())
				.then(statistics_query(cra))
				.then(statistics_populateAll(cra));
		final var stats = literal("stats").requires(scs -> scs.hasPermission(2))
				.then(statistics_edit(cra))
				.then(statistics_clear())
				.then(statistics_query(cra))
				.then(statistics_populateAll(cra));
		
		//register the command
		dispatcher.register(statistics);
		dispatcher.register(stats);
	}
	// --------------------------------------------------
	private static ArgumentBuilder<CommandSourceStack, ?> statistics_edit(CommandBuildContext cra)
	{
		return literal("edit")
				.then(argument("targets", EntityArgument.players())
						.then(argument("stat_type", ResourceArgument.resource(cra, Registries.STAT_TYPE))
								.then(argument("stat", ResourceLocationArgument.id()).suggests(SUGGEST_STAT)
										.then(literal("set")
												.then(argument("value", IntegerArgumentType.integer(0))
														.executes(ctx -> execute_edit(ctx, true))
														)
												)
										.then(literal("increase")
												.then(argument("value", IntegerArgumentType.integer())
														.executes(ctx -> execute_edit(ctx, false))
														)
												)
										)
								)
						);
	}
	private static ArgumentBuilder<CommandSourceStack, ?> statistics_clear()
	{
		return literal("clear")
				.then(argument("targets", EntityArgument.players())
						.executes(ctx -> execute_clear(ctx)));
	}
	private static ArgumentBuilder<CommandSourceStack, ?> statistics_query(CommandBuildContext cra)
	{
		return literal("query")
				.then(argument("target", EntityArgument.player())
						.then(argument("stat_type", ResourceArgument.resource(cra, Registries.STAT_TYPE))
								.then(argument("stat", ResourceLocationArgument.id()).suggests(SUGGEST_STAT)
										.executes(ctx -> execute_query(ctx))
										)
								)
					);
	}
	private static ArgumentBuilder<CommandSourceStack, ?> statistics_populateAll(CommandBuildContext cra)
	{
		return literal("danger_zone_populate_all_stats").requires(scs -> BetterStatsConfig.DEBUG_MODE)
				.then(argument("target", EntityArgument.player())
						.executes(ctx -> execute_populateAll(ctx))
						);
	}
	// --------------------------------------------------
	/**
	 * Suggests {@link Stat} registry entries.<br/>
	 * Credit: https://github.com/TheCSMods/mc-better-stats/issues/102#issuecomment-2045698948
	 * @apiNote The context should define a {@link StatType} with the name "stat_type".
	 */
	private static SuggestionProvider<CommandSourceStack> SUGGEST_STAT = (context, builder) ->
	{
		//try to obtain the type of stats we want to be suggesting
		@Nullable StatType<?> statType = null;
		try { statType = ResourceArgument.getResource(context, "stat_type", Registries.STAT_TYPE).value(); }
		catch(Exception e) {}
		
		//if a stat type was not provided properly or at all, use default behavior
		if(statType == null) return ResourceLocationArgument.id().listSuggestions(context, builder);
		
		//next up, after obtaining the target stat type, list the suggestions
		@Nullable Iterable<ResourceLocation> suggestions = statType.getRegistry().registryKeySet()
				.stream().map(ResourceKey::location).toList();
		return SharedSuggestionProvider.suggest(
				StreamSupport.stream(suggestions.spliterator(), false).map(Objects::toString),
				builder);
	};
	// ==================================================
	@SuppressWarnings("unchecked")
	private static int execute_edit(CommandContext<CommandSourceStack> context, boolean setOrIncrease)
	{
		try
		{
			//get parameter values
			final var arg_targets = EntityArgument.getPlayers(context, "targets");
			final var arg_stat_type = (StatType<Object>)ResourceArgument.getResource(context, "stat_type", Registries.STAT_TYPE).value();
			final var arg_stat = ResourceLocationArgument.getId(context, "stat");
			final int arg_value = IntegerArgumentType.getInteger(context, "value");
			
			final var stat_object = arg_stat_type.getRegistry().getOptional(arg_stat).orElse(null);
			Objects.requireNonNull(stat_object, "Registry entry '" + arg_stat + "' does not exist for registry '" + arg_stat_type.getRegistry() + "'.");
			final var stat = arg_stat_type.get(stat_object);
			
			//execute
			final AtomicInteger affected = new AtomicInteger();
			for(final var target : arg_targets)
			{
				//null check
				if(target == null) continue;
				
				//set stat value
				if(setOrIncrease) target.getStats().setValue(target, stat, arg_value);
				else target.getStats().increment(target, stat, arg_value);
				affected.incrementAndGet();
				
				//update the client
				target.getStats().sendStats(target);
			}
			
			//send feedback
			context.getSource().sendSuccess(() -> BST.cmd_stats_edit_out(
					TextUtils.literal("[" + BuiltInRegistries.STAT_TYPE.getKey(arg_stat_type) + " / " + arg_stat + "]"),
					TextUtils.literal(Integer.toString(affected.get()))
				), false);
			
			//return affected count, so command blocks and data-packs can know it
			return affected.get();
		}
		catch(CommandSyntaxException | IllegalStateException | NullPointerException e)
		{
			handleError(context, e);
			return -1;
		}
	}
	private static int execute_clear(CommandContext<CommandSourceStack> context)
	{
		try
		{
			//get parameter values
			final var targets = EntityArgument.getPlayers(context, "targets");

			//execute
			final AtomicInteger affected = new AtomicInteger();
			for(final var target : targets)
			{
				//null check
				if(target == null) continue;
				
				//clear statistics
				((AccessorStatHandler)target.getStats()).getStatMap().clear();
				affected.incrementAndGet();
				
				//disconnect the player because that's the only way to update the client
				target.connection.disconnect(TextUtils.literal("")
						.append(TEXT_CLEAR_KICK)
						.append("\n\n[EN]: Your statistics were cleared, which requires you to disconnect and re-join."));
			}
			
			//send feedback
			context.getSource().sendSuccess(() -> BST.cmd_stats_clear_out(TextUtils.literal(Integer.toString(affected.get()))), false);
			
			//return affected count, so command blocks and data-packs can know it
			return affected.get();
		}
		catch(CommandSyntaxException e)
		{
			handleError(context, e);
			return -1;
		}
	}
	@SuppressWarnings("unchecked")
	private static int execute_query(CommandContext<CommandSourceStack> context)
	{
		try
		{
			//get parameter values
			final var arg_target = EntityArgument.getPlayer(context, "target");
			if(arg_target == null) throw new SimpleCommandExceptionType(TextUtils.literal("Player not found.")).create();
			final var arg_stat_type = (StatType<Object>)ResourceArgument.getResource(context, "stat_type", Registries.STAT_TYPE).value();
			final var arg_stat = ResourceLocationArgument.getId(context, "stat");

			final var stat_object = arg_stat_type.getRegistry().getOptional(arg_stat).orElse(null);
			Objects.requireNonNull(stat_object, "Registry entry '" + arg_stat + "' does not exist for registry '" + arg_stat_type.getRegistry() + "'.");
			
			final var stat = arg_stat_type.get(stat_object);
			final int statValue = arg_target.getStats().getValue(stat);
			
			//execute
			context.getSource().sendSuccess(() -> BST.cmd_stats_query_out(
					arg_target.getDisplayName(),
					TextUtils.literal("[" + BuiltInRegistries.STAT_TYPE.getKey(arg_stat_type) + " / " + arg_stat + "]"),
					TextUtils.literal(Integer.toString(statValue))
				), false);
			return statValue;
		}
		catch(CommandSyntaxException e)
		{
			handleError(context, e);
			return -1;
		}
	}
	@SuppressWarnings("unchecked")
	private static int execute_populateAll(CommandContext<CommandSourceStack> context)
	{
		try
		{
			//get parameter values
			final var arg_target = EntityArgument.getPlayer(context, "target");
			if(arg_target == null) throw new SimpleCommandExceptionType(TextUtils.literal("Player not found.")).create();
			
			//obtain stats provider and populate
			int affected = 0;
			final var sp = arg_target.getStats();
			for(final var statType : (Registry<StatType<Object>>)(Object)BuiltInRegistries.STAT_TYPE)
			{
				for(final var stat : (Registry<Object>)statType.getRegistry())
				{
					final var s = statType.get(stat);
					if(sp.getValue(s) != 0) continue;
					sp.setValue(arg_target, s, 1);
					affected++;
				}
			}
			
			//output and return
			context.getSource().sendSuccess(() -> TextUtils.translatable("gui.done"), false);
			return affected;
		}
		catch(CommandSyntaxException e)
		{
			handleError(context, e);
			return -1;
		}
	}
	// ==================================================
}