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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	/// The block the held rumble is for, so that ticks of mining the same block keep the one playback going.
	@Unique private BlockPos controlify$rumblePos;
	@Unique private Block controlify$rumbleBlock;

	@Inject(
			method = "lambda$startDestroyBlock$1",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V")
	)
	private void onStartBreakingBlock(
		CallbackInfoReturnable<Packet<?>> cir,
		@Local(argsOnly = true, name = "state") BlockState state,
		@Local(argsOnly = true, name = "pos") BlockPos pos
	) {
		startRumble(state, pos);
	}

	@Inject(
			method = "lambda$startDestroyBlock$1",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z")
	)
	private void onInstabreakBlockSurvival(
		CallbackInfoReturnable<Packet<?>> cir,
		@Local(argsOnly = true, name = "state") BlockState state,
		@Local(argsOnly = true, name = "pos") BlockPos pos
	) {
		startRumble(state, pos);
		// won't stop until 1 tick
		stopRumble();
	}

	/// Creative mode takes its own branch that never reaches the survival lambda above.
	@Inject(
			method = "startDestroyBlock",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", ordinal = 0)
	)
	private void onStartBreakingBlockCreative(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir, @Local BlockState state) {
		creativeBreak(state, pos);
	}

	/// Creative mode breaks every few ticks while the button is held.
	@Inject(
			method = "continueDestroyBlock",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", ordinal = 0)
	)
	private void onContinueBreakingBlockCreative(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir, @Local BlockState state) {
		creativeBreak(state, pos);
	}

	@Inject(method = "stopDestroyBlock", at = @At("RETURN"))
	private void onStopBreakingBlock(CallbackInfo ci) {
		stopRumble();
	}

	@Inject(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", ordinal = 1))
	private void onFinishBreakingBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		stopRumble();
	}

	@ModifyExpressionValue(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
	private boolean onContinueBreakingBlock(boolean isAir, @Local(argsOnly = true, name = "pos") BlockPos pos, @Local(name = "state") BlockState state) {
		if (isAir) {
			stopRumble();
		} else {
			// still mining this block: keep the held rumble alive
			startRumble(state, pos);
		}

		return isAir;
	}

	@Unique private void creativeBreak(BlockState state, BlockPos pos) {
		if (state.isAir()) {
			return;
		}
		// breaks instantly, so it is felt as a single short pulse like an instantly mined block
		startRumble(state, pos);
		stopRumble();
	}

	@Unique private void startRumble(BlockState state, BlockPos pos) {
		// a new block starts a fresh playback and the previous one ends; the same block keeps its playback
		if (!pos.equals(this.controlify$rumblePos) || state.getBlock() != this.controlify$rumbleBlock) {
			BuiltinFeedback.BLOCK_BREAK.release();
			this.controlify$rumblePos = pos.immutable();
			this.controlify$rumbleBlock = state.getBlock();
		}
		BuiltinFeedback.BLOCK_BREAK.hold(new BuiltinFeedback.BlockBreak(state.getBlock().defaultDestroyTime()));
	}

	@Unique private void stopRumble() {
		this.controlify$rumblePos = null;
		this.controlify$rumbleBlock = null;
		BuiltinFeedback.BLOCK_BREAK.release();
	}
}
