/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.explosion;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
	private boolean onLightningStrike(boolean client) {
		if (client) {
			BuiltinFeedback.LIGHTNING.fireAt(((Entity) (Object) this).position(), null);
		}
		return client;
	}
}
