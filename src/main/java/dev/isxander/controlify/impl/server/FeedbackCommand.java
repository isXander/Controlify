/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// `/feedback play <effect> [origin <pos>|origin entity <entity>] [groups <group>...] [param <name> <value>]... [to <targets>]`
///
/// Built like vanilla's `/execute`: every clause is a literal that redirects back to the same set
/// of clauses, so they can be chained in any order and each is optional. Brigadier gives the
/// finished command only the arguments of its own segment, so each clause records into a
/// [Pending] as the chain runs, and the end of the chain reads it. A chain always starts with the
/// effect clause, which begins a fresh [Pending]; commands run one at a time on the server thread.
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

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		// The clauses redirect back here. It is never registered: only its children are used.
		CommandNode<CommandSourceStack> clauses = Commands.literal("clauses").build();
		// After a group, another group can follow without repeating `groups`.
		CommandNode<CommandSourceStack> moreGroups = Commands.literal("groups").build();

		CommandNode<CommandSourceStack> origin = Commands.literal("origin")
				.then(Commands.argument("pos", Vec3Argument.vec3(true))
						.executes(FeedbackCommand::run)
						.redirect(clauses, context -> {
							Vec3 pos = Vec3Argument.getVec3(context, "pos");
							Pending pending = pending(context);
							pending.origin = new FeedbackEffectPacket.Origin.Position(pos);
							return context.getSource();
						}))
				.then(Commands.literal("entity")
						.then(Commands.argument("entity", EntityArgument.entity())
								.executes(FeedbackCommand::run)
								.redirect(clauses, context -> {
									Entity entity = EntityArgument.getEntity(context, "entity");
									Pending pending = pending(context);
									pending.origin = new FeedbackEffectPacket.Origin.EntityOrigin(entity.getId());
									return context.getSource();
								})))
				.build();

		CommandNode<CommandSourceStack> groups = Commands.literal("groups")
				.then(Commands.argument("group", IdentifierArgument.id())
						.executes(FeedbackCommand::run)
						.redirect(moreGroups, FeedbackCommand::addGroup))
				.build();

		CommandNode<CommandSourceStack> param = Commands.literal("param")
				.then(Commands.argument("name", StringArgumentType.word())
						.then(Commands.argument("value", FloatArgumentType.floatArg())
								.executes(FeedbackCommand::run)
								.redirect(clauses, context -> {
									pending(context).params.put(
											StringArgumentType.getString(context, "name"),
											FloatArgumentType.getFloat(context, "value")
									);
									return context.getSource();
								})))
				.build();

		CommandNode<CommandSourceStack> to = Commands.literal("to")
				.then(Commands.argument("targets", EntityArgument.players())
						.executes(FeedbackCommand::run)
						.redirect(clauses, context -> {
							pending(context).targets = EntityArgument.getPlayers(context, "targets");
							return context.getSource();
						}))
				.build();

		for (CommandNode<CommandSourceStack> clause : List.of(origin, groups, param, to)) {
			clauses.addChild(clause);
			moreGroups.addChild(clause);
		}
		// `groups a b c`: each following group is just an identifier, tried after the literals above
		moreGroups.addChild(Commands.argument("group", IdentifierArgument.id())
				.executes(FeedbackCommand::run)
				.redirect(moreGroups, FeedbackCommand::addGroup)
				.build());

		dispatcher.register(
				Commands.literal("feedback")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.literal("play")
								.then(Commands.argument("effect", IdentifierArgument.id())
										.executes(FeedbackCommand::run)
										.redirect(clauses, context -> {
											current = new Pending();
											current.effect = IdentifierArgument.getId(context, "effect");
											return context.getSource();
										})))
		);
	}

	private static CommandSourceStack addGroup(CommandContext<CommandSourceStack> context) {
		Identifier group = IdentifierArgument.getId(context, "group");
		pending(context).groups.add(group);
		return context.getSource();
	}

	private static Pending pending(CommandContext<CommandSourceStack> context) {
		if (current == null) {
			current = new Pending();
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
			pending.params.put(StringArgumentType.getString(context, "name"), FloatArgumentType.getFloat(context, "value"));
		}
		if (has(context, "targets")) {
			pending.targets = EntityArgument.getPlayers(context, "targets");
		}
	}

	private static boolean has(CommandContext<CommandSourceStack> context, String name) {
		try {
			context.getArgument(name, Object.class);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
