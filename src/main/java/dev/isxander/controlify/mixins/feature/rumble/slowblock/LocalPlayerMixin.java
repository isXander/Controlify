/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.slowblock;

import com.mojang.authlib.GameProfile;
import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
	@Shadow
	protected abstract boolean isMoving();

	@Shadow public ClientInput input;

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
			BuiltinFeedback.SLOW_BLOCK.hold(new BuiltinFeedback.Movement(this.input.getMoveVector().length()));
		} else {
			BuiltinFeedback.SLOW_BLOCK.release();
		}
	}
}
