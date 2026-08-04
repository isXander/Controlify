/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.explosion;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Unique private static final float RUMBLE_FALLOFF_DISTANCE = 32f;
	@Unique private static final float FULL_STRENGTH_EXPLOSION_RADIUS = 6f;

	@Inject(method = "handleExplosion", at = @At("RETURN"))
	private void onClientExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
		float magnitude = calculateMagnitude(packet);
		if (magnitude <= 0f) {
			return;
		}

		ControlifyApi.get().playRumbleEffect(
				RumbleSource.WORLD,
				BasicRumbleEffect.seq(
						BasicRumbleEffect.constant(magnitude, magnitude, 2), // initial boom
						BasicRumbleEffect.byTime(t -> {
							float decay = 1f - t;
							decay *= decay;
							return new RumbleState(
									magnitude * 0.8f * decay,
									magnitude * 0.2f * decay
							);
						}, 8) // low-frequency aftershock
				)
		);
	}

	@Unique private float calculateMagnitude(ClientboundExplodePacket packet) {
		double x = packet.center().x();
		double y = packet.center().y();
		double z = packet.center().z();
		float radius = Math.max(packet.radius(), 0f);

		double distance = Math.sqrt(Minecraft.getInstance().player.distanceToSqr(x, y, z));
		float surfaceDistance = Math.max((float) distance - radius, 0f);
		if (surfaceDistance >= RUMBLE_FALLOFF_DISTANCE) {
			return 0f;
		}

		float proximity = 1f - surfaceDistance / RUMBLE_FALLOFF_DISTANCE;
		float radiusMagnitude = Math.min(
				(float) Math.sqrt(radius / FULL_STRENGTH_EXPLOSION_RADIUS),
				1f
		);
		return proximity * proximity * proximity * radiusMagnitude;
	}
}
