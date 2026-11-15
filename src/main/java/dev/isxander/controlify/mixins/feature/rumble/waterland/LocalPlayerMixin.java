/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.waterland;

import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import java.util.Objects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends PlayerMixin {
	protected LocalPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void splashRumble(CallbackInfo ci) {
		if (isSpectator()) {
			return;
		}

		Entity entity = Objects.requireNonNullElse(this.getControllingPassenger(), this);
		float f = entity == this ? 0.2F : 0.9F;
		Vec3 vec3 = entity.getDeltaMovement();
		float impactForce = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * 0.2F + vec3.y * vec3.y + vec3.z * vec3.z * 0.2F) * f);

		// too gentle to feel
		if (impactForce >= 0.05f) {
			BuiltinFeedback.WATER_LAND.fire(new BuiltinFeedback.WaterImpact(impactForce));
		}
	}
}
