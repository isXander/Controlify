/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.slowblock;

import com.mojang.authlib.GameProfile;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
	@Shadow
	protected abstract boolean isMoving();

	@Shadow public ClientInput input;

	@Unique private RumbleEffectInstance slowBlockRumble = null;

	public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(
			method = "aiStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/ClientInput;tick()V"
			)
	)
	private void manageSlowBlockRumble(CallbackInfo ci) {
		float speed = this.getBlockSpeedFactor();

		if (speed < 1f && isMoving()) {
			ensureRumbleStarted();
		} else {
			ensureRumbleStopped();
		}
	}

	@Unique private void ensureRumbleStarted() {
		if (slowBlockRumble == null || slowBlockRumble.hasFinished()) {
			var effect = RumbleEffect.continuous()
					.byTick(i -> {
						float movementAmount = input.getMoveVector().length();
						return new RumbleState(0.3f * movementAmount, 0.5f * movementAmount);
					})
					.timeout(100);
			slowBlockRumble = ControlifyApi.get().playRumbleEffect(CUtil.rl("slow_block"), effect).orElse(null);
		} else {
			slowBlockRumble.keepAlive();
		}
	}

	@Unique private void ensureRumbleStopped() {
		if (slowBlockRumble != null && !slowBlockRumble.hasFinished()) {
			slowBlockRumble.stop();
		}
	}
}
