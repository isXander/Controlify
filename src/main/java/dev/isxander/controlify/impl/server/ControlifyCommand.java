/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/// The `/controlify` root command. Every Controlify command is a subcommand of it,
/// so none of them can clash with commands from vanilla or other mods.
public final class ControlifyCommand {
	private ControlifyCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("controlify")
				.then(FeedbackCommand.build()));
	}
}
