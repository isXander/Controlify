/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.fishing;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingHookMixin {
	@Shadow private boolean biting;

	@Unique private boolean isLocalPlayerHook;

	@ModifyExpressionValue(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;get(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;", ordinal = 1))
	private Object onBitingStateUpdated(Object bitingObj) {
		var biting = (boolean) bitingObj;
		if (isLocalPlayerHook) {
			if (biting && !this.biting) {
				BuiltinFeedback.FISHING_BITE.hold();
			} else if (!biting && this.biting) {
				BuiltinFeedback.FISHING_BITE.release();
			}
		}
		return biting;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void keepBitingRumbleHeld(CallbackInfo ci) {
		if (isLocalPlayerHook && this.biting) {
			BuiltinFeedback.FISHING_BITE.hold();
		}
	}

	@Inject(method = "onClientRemoval", at = @At("RETURN"))
	private void onClientRemoval(CallbackInfo ci) {
		if (isLocalPlayerHook) {
			BuiltinFeedback.FISHING_BITE.release();
		}
	}

	@Inject(method = "setOwner", at = @At("RETURN"))
	private void onOwnerSet(@Nullable Entity owner, CallbackInfo ci) {
		isLocalPlayerHook = owner instanceof LocalPlayer;
	}
}
