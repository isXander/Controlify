/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.isxander.controlify.example.Zoom;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {
	@ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
	private float zoom(float fov) {
		return Zoom.isZooming() ? fov * Zoom.FOV_MULTIPLIER : fov;
	}
}
