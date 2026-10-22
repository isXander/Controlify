/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.explosion;

import dev.isxander.controlify.impl.Controlify;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
	private boolean onLightningStrike(boolean client) {
		if (client) {
			Controlify.instance().playRumbleEffect(
					CUtil.rl("lightning"),
					RumbleEffect.finite().constant(1f, 0.2f, 6) // initial boom
							.then(RumbleEffect.finite().byTime(t -> new RumbleState(0f, 1 - t*0.2f), 10)) // explosion
			);
		}
		return client;
	}
}
