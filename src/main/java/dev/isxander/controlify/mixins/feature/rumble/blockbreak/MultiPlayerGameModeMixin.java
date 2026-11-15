/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.blockbreak;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Inject(
			method = "lambda$startDestroyBlock$1",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V")
	)
	private void onStartBreakingBlock(
		CallbackInfoReturnable<Packet<?>> cir,
		@Local(argsOnly = true, name = "state") BlockState state
	) {
		startRumble(state);
	}

	@Inject(
			method = "lambda$startDestroyBlock$1",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z")
	)
	private void onInstabreakBlockSurvival(
		CallbackInfoReturnable<Packet<?>> cir,
		@Local(argsOnly = true, name = "state") BlockState state
	) {
		startRumble(state);
		// won't stop until 1 tick
		BuiltinFeedback.BLOCK_BREAK.release();
	}

	@Inject(method = "stopDestroyBlock", at = @At("RETURN"))
	private void onStopBreakingBlock(CallbackInfo ci) {
		BuiltinFeedback.BLOCK_BREAK.release();
	}

	@Inject(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", ordinal = 1))
	private void onFinishBreakingBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		BuiltinFeedback.BLOCK_BREAK.release();
	}

	@ModifyExpressionValue(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
	private boolean onContinueBreakingBlock(boolean isAir, @Local(name = "state") BlockState state) {
		if (isAir) {
			BuiltinFeedback.BLOCK_BREAK.release();
		} else {
			// still mining this block: keep the held rumble alive
			startRumble(state);
		}

		return isAir;
	}

	@Unique private void startRumble(BlockState state) {
		// a new block starts a fresh playback; the previous one ends
		BuiltinFeedback.BLOCK_BREAK.release();
		BuiltinFeedback.BLOCK_BREAK.hold(new BuiltinFeedback.BlockBreak(state.getBlock().defaultDestroyTime()));
	}
}
