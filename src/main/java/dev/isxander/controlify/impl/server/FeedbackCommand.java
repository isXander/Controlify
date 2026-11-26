/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.server;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import dev.isxander.controlify.impl.platform.network.SidedNetworkApi;
import dev.isxander.controlify.impl.server.packets.FeedbackEffectPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
//? if >=26.4 {
import net.minecraft.commands.arguments.FloatArgument;
import net.minecraft.commands.arguments.StringArgument;
//?} else {
/*import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
*///?}
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
//? if >=26.4 {
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
//?}
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// `/controlify feedback play <effect> [origin <pos>|origin entity <entity>] [groups <group>...] [param <name> <value>]... [to <targets>]`
///
/// Built like vanilla's `/execute`: the clauses are children of the effect argument, and every
/// clause redirects back to it, so they can be chained in any order and each is optional. A group
/// list redirects back to the `groups` literal instead, so further groups need no repeated keyword.
/// Redirect targets must be registered nodes, as the command tree is copied node by node.
/// Brigadier gives the finished command only the arguments of its own segment, so each clause
/// records into a [Pending] as the chain runs, and the end of the chain reads it. Only the first
/// segment holds the effect, so it begins a fresh [Pending]; commands run one at a time on the
/// server thread.
public final class FeedbackCommand {
	private static final SimpleCommandExceptionType NO_TARGETS = new SimpleCommandExceptionType(
			Component.translatable("controlify.command.feedback.no_targets"));

	/// What the clauses of the command that is running have collected so far.
	private static Pending current;

	private static final class Pending {
		Identifier effect;
		FeedbackEffectPacket.Origin origin;
		final List<Identifier> groups = new ArrayList<>();
		final Map<String, Float> params = new LinkedHashMap<>();
		Collection<ServerPlayer> targets;
	}

	private FeedbackCommand() {
	}

	/// The `feedback` subcommand of [ControlifyCommand].
	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		// Holds the clauses, and every clause redirects back here.
		CommandNode<CommandSourceStack> effect = Commands.argument("effect", IdentifierArgument.id())
				.executes(FeedbackCommand::run)
				.build();
		// After a group, another group can follow without repeating `groups`.
		CommandNode<CommandSourceStack> groups = Commands.literal("groups").build();

		CommandNode<CommandSourceStack> origin = Commands.literal("origin")
				.then(Commands.argument("pos", Vec3Argument.vec3(true))
						.executes(FeedbackCommand::run)
						.redirect(effect, context -> {
							Vec3 pos = Vec3Argument.getVec3(context, "pos");
							Pending pending = pending(context);
							pending.origin = new FeedbackEffectPacket.Origin.Position(pos);
							return context.getSource();
						}))
				.then(Commands.literal("entity")
						.then(Commands.argument("entity", EntityArgument.entity())
								.executes(FeedbackCommand::run)
								.redirect(effect, context -> {
									Entity entity = EntityArgument.getEntity(context, "entity");
									Pending pending = pending(context);
									pending.origin = new FeedbackEffectPacket.Origin.EntityOrigin(entity.getId());
									return context.getSource();
								})))
				.build();

		CommandNode<CommandSourceStack> param = Commands.literal("param")
				.then(Commands.argument("name", word())
						.then(Commands.argument("value", floatArg())
								.executes(FeedbackCommand::run)
								.redirect(effect, context -> {
									pending(context).params.put(
											getWord(context, "name"),
											getFloat(context, "value")
									);
									return context.getSource();
								})))
				.build();

		CommandNode<CommandSourceStack> to = Commands.literal("to")
				.then(Commands.argument("targets", EntityArgument.players())
						.executes(FeedbackCommand::run)
						.redirect(effect, context -> {
							pending(context).targets = EntityArgument.getPlayers(context, "targets");
							return context.getSource();
						}))
				.build();

		// `groups a b c`: each group is an identifier, tried after the literal clauses below
		groups.addChild(Commands.argument("group", IdentifierArgument.id())
				.executes(FeedbackCommand::run)
				.redirect(groups, FeedbackCommand::addGroup)
				.build());
		for (CommandNode<CommandSourceStack> clause : List.of(origin, param, to)) {
			effect.addChild(clause);
			groups.addChild(clause);
		}
		effect.addChild(groups);

		return Commands.literal("feedback")
				//? if >=26.4 {
				.requires(Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
				//?} else {
				//.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				//?}
				.then(Commands.literal("play").then(effect));
	}

	private static CommandSourceStack addGroup(CommandContext<CommandSourceStack> context) {
		Identifier group = IdentifierArgument.getId(context, "group");
		pending(context).groups.add(group);
		return context.getSource();
	}

	private static Pending pending(CommandContext<CommandSourceStack> context) {
		// the first segment of a chain is the only one with the effect
		if (current == null || has(context, "effect")) {
			current = new Pending();
			if (has(context, "effect")) {
				current.effect = IdentifierArgument.getId(context, "effect");
			}
		}
		return current;
	}

	private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		// the last clause's own arguments are in this context; a command with only an effect has no earlier ones
		Pending pending = has(context, "effect") || current == null ? new Pending() : current;
		current = null;
		applyFinalSegment(context, pending);

		Collection<ServerPlayer> targets = pending.targets;
		if (targets == null) {
			ServerPlayer self = source.getPlayer();
			if (self == null) {
				throw NO_TARGETS.create();
			}
			targets = List.of(self);
		}

		FeedbackEffectPacket packet = new FeedbackEffectPacket(pending.effect, pending.origin, pending.groups, pending.params);
		for (ServerPlayer player : targets) {
			SidedNetworkApi.S2C().sendPacket(player, FeedbackEffectPacket.CHANNEL, packet);
		}

		Identifier effect = pending.effect;
		int count = targets.size();
		source.sendSuccess(
				() -> count == 1
						? Component.translatable("controlify.command.feedback.play.single", effect.toString())
						: Component.translatable("controlify.command.feedback.play.multiple", effect.toString(), count),
				true
		);
		return count;
	}

	/// The clause that ends the chain has no redirect after it, so its arguments are only in this context.
	private static void applyFinalSegment(CommandContext<CommandSourceStack> context, Pending pending) throws CommandSyntaxException {
		if (has(context, "effect")) {
			pending.effect = IdentifierArgument.getId(context, "effect");
		}
		if (has(context, "pos")) {
			Vec3 pos = Vec3Argument.getVec3(context, "pos");
			pending.origin = new FeedbackEffectPacket.Origin.Position(pos);
		}
		if (has(context, "entity")) {
			pending.origin = new FeedbackEffectPacket.Origin.EntityOrigin(EntityArgument.getEntity(context, "entity").getId());
		}
		if (has(context, "group")) {
			pending.groups.add(IdentifierArgument.getId(context, "group"));
		}
		if (has(context, "name") && has(context, "value")) {
			pending.params.put(getWord(context, "name"), getFloat(context, "value"));
		}
		if (has(context, "targets")) {
			pending.targets = EntityArgument.getPlayers(context, "targets");
		}
	}

	// 26.4 only accepts vanilla's wrappers around Brigadier's argument types in the command tree
	//? if >=26.4 {
	private static ArgumentType<String> word() {
		return StringArgument.word();
	}

	private static ArgumentType<Float> floatArg() {
		return FloatArgument.floatArg();
	}

	private static String getWord(CommandContext<CommandSourceStack> context, String name) {
		return StringArgument.getString(context, name);
	}

	private static float getFloat(CommandContext<CommandSourceStack> context, String name) {
		return FloatArgument.getFloat(context, name);
	}
	//?} else {
	/*private static ArgumentType<String> word() {
		return StringArgumentType.word();
	}

	private static ArgumentType<Float> floatArg() {
		return FloatArgumentType.floatArg();
	}

	private static String getWord(CommandContext<CommandSourceStack> context, String name) {
		return StringArgumentType.getString(context, name);
	}

	private static float getFloat(CommandContext<CommandSourceStack> context, String name) {
		return FloatArgumentType.getFloat(context, name);
	}
	*///?}

	private static boolean has(CommandContext<CommandSourceStack> context, String name) {
		try {
			context.getArgument(name, Object.class);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
