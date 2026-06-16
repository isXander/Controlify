package dev.isxander.controlify.mixins.core;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.utils.animation.impl.Animator;
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

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;handleAccumulatedMovement()V"))
    private void doPlayerLook(boolean advanceGameTime, CallbackInfo ci) {
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
        Controlify.instance().getControllerManager().ifPresent(ControllerManager::close);
    }

    @Inject(
            method = "renderFrame",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"
            )
    )
    private void tickAnimator(CallbackInfo ci) {
        Animator.INSTANCE.tick(getTickDelta());
    }

    @Unique
    private float getTickDelta() {
        return getDeltaTracker().getGameTimeDeltaTicks();
    }
}
