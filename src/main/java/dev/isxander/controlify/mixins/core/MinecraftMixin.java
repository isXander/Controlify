package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.utils.InitialScreenRegistryDuck;
import dev.isxander.controlify.utils.MouseMinecraftCallNotifier;
import dev.isxander.controlify.utils.animation.impl.Animator;
import net.minecraft.CrashReport;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements InitialScreenRegistryDuck {
    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow public abstract DeltaTracker getDeltaTracker();

    @Shadow @Final public MouseHandler mouseHandler;
    @Shadow @Nullable public Screen screen;

    @Shadow
    public abstract void emergencySaveAndCrash(CrashReport crashReport);

    @Unique private final List<Function<Runnable, Screen>> initialScreenCallbacks = new ArrayList<>();
    @Unique private boolean initialScreensHappened = false;

    // Ideally, this would be done in MouseHandler#releaseMouse, but moving
    // the mouse before the screen init is bad, because some mods (e.g. PuzzleLib)
    // have custom mouse events that call into screens, events that have not been
    // initialised yet in Screen#init. Causing NPEs and many strange issues.
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;releaseMouse()V"))
    private void notifyInjectionToNotRun(Screen screen, CallbackInfo ci) {
        ((MouseMinecraftCallNotifier) mouseHandler).controlify$imFromMinecraftSetScreen();
    }

    /**
     * Without this, the mouse would be left in the middle of the
     * screen, hovering over whatever is there which would look wrong
     * as there is a focus as well.
     */
    @Inject(
            method = "setScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;init(II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void hideMouseAfterRelease(Screen screen, CallbackInfo ci) {
        if (ControlifyApi.get().currentInputMode().isController()) {
            Controlify.instance().hideMouse(true, true);
        }
    }

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

    @WrapMethod(method = "addInitialScreens")
    private boolean injectCustomInitialScreens(List<Function<Runnable, Screen>> screens, Operation<Boolean> original) {
        boolean result = original.call(screens);
        screens.addAll(initialScreenCallbacks);
        initialScreensHappened = true;
        return result;
    }

    @Unique
    private float getTickDelta() {
        return getDeltaTracker().getGameTimeDeltaTicks();
    }

    @Override
    public void controlify$registerInitialScreen(Function<Runnable, Screen> screenFactory) {
        if (initialScreensHappened) {
            Screen lastScreen = this.screen;
            setScreen(screenFactory.apply(() -> setScreen(lastScreen)));
        } else {
            initialScreenCallbacks.add(screenFactory);
        }
    }
}
