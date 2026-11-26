/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.waterland;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Injects into the definition of the splash effect so subclass mixins can override the handler,
/// no matter which subclass (if any) overrides the method or the path that leads to it.
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "doWaterSplashEffect", at = @At("HEAD"))
	protected void splashRumble(CallbackInfo ci) {
	}
}
