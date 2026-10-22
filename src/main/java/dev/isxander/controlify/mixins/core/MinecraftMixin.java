/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controllermanager.ControllerCoordinator;
import dev.isxander.controlify.impl.utils.animation.impl.Animator;
import net.minecraft.CrashReport;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow public abstract DeltaTracker getDeltaTracker();

	@Shadow
	public abstract void emergencySaveAndCrash(CrashReport partialReport);

	@Inject(method = "onGameLoadFinished", at = @At("RETURN"))
	private void initControlifyNow(CallbackInfo ci) {
		try {
			Controlify.instance().initializeControlify();
		} catch (Throwable t) {
			CrashReport report = CrashReport.forThrowable(t, "Failed to initialize Controlify");

			// Further up the stack, any throwable is caught, including ReportedException,
			// so we need to manually crash the game here.
			emergencySaveAndCrash(report);
		}
	}

	@WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runTick(Z)V"))
	private void wrapUpdate(Minecraft instance, boolean advanceGameTime, Operation<Void> original) {
		Controlify.instance().update(getTickDelta());
		original.call(instance, advanceGameTime);
		Controlify.instance().postUpdate(getTickDelta());
	}

	@Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;handleAccumulatedMovement()V"))
	private void doPlayerLook(CallbackInfo ci) {
		Controlify.instance().inGameInputHandler().ifPresent(ih -> ih.processPlayerLook(getTickDelta()));
	}

	@Inject(
			method = "close",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/telemetry/ClientTelemetryManager;close()V"
			)
	)
	private void onMinecraftClose(CallbackInfo ci) {
		Controlify.instance().getControllerCoordinator().ifPresent(ControllerCoordinator::close);
	}

	@Inject(
			method = "renderFrame",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/GameRenderer;extract(Lnet/minecraft/client/DeltaTracker;Z)V"
			)
	)
	private void tickAnimator(CallbackInfo ci) {
		Animator.INSTANCE.tick(getTickDelta());
	}

	@Unique private float getTickDelta() {
		return getDeltaTracker().getGameTimeDeltaTicks();
	}
}
