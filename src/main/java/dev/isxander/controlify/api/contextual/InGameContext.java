/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.feature.guide.ingame.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public record InGameContext(
		Minecraft client,
		LocalPlayer player,
		ClientLevel level,
		HitResult hitResult,
		ControllerEntity controller,
		GuideVerbosity verbosity
) implements Context {
	@ApiStatus.Internal
	public static InGameContext create(Minecraft minecraft, ControllerEntity controller) {
		var hitResult = minecraft.hitResult;
		if (hitResult == null) {
			((MinecraftAccessor) minecraft).controlify$invokePick(1f);
			hitResult = minecraft.hitResult;
		}

		return new InGameContext(
				minecraft,
				Objects.requireNonNull(minecraft.player, "player"),
				Objects.requireNonNull(minecraft.level, "level"),
				hitResult,
				controller,
				controller.settings().generic.guide.verbosity
		);
	}
}
