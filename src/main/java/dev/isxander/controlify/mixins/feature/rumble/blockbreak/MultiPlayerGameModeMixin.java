/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.blockbreak;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.Easings;
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

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Unique private RumbleEffectInstance blockBreakRumble = null;
	@Unique private AtomicBoolean blockBreakStopped;

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
		stopRumble();
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
	private boolean onAbortBreakingBlock(boolean original) {
		if (original)
			stopRumble();

		return original;
	}

	@Unique private void startRumble(BlockState state) {
		stopRumble();

		var stopped = new AtomicBoolean();
		blockBreakStopped = stopped;
		var effect = RumbleEffect.continuous()
				.byTick(tick -> new RumbleState(
						0.02f + (float)Easings.easeInQuad(Math.min(1, state.getBlock().defaultDestroyTime() / 20f)) * 0.25f,
						0.01f
				))
				.minTime(1)
				.stopCondition(stopped::get);

		blockBreakRumble = ControlifyApi.get().playRumbleEffect(CUtil.rl("block_break"), effect).orElse(null);
	}

	@Unique private void stopRumble() {
		if (blockBreakRumble != null) {
			blockBreakStopped.set(true);
			blockBreakRumble = null;
		}
	}
}
