/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;

public record InGameCtx(
		Minecraft client,
		LocalPlayer player,
		ClientLevel level,
		HitResult hitResult,
		ControllerEntity controller,
		GuideVerbosity verbosity
) implements FactCtx {
}
