package dev.isxander.controlify.mixins.core;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.utils.InitialScreenRegistryDuck;
import dev.isxander.controlify.utils.MouseMinecraftCallNotifier;
import dev.isxander.controlify.utils.animation.impl.Animator;
import net.minecraft.CrashReport;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements InitialScreenRegistryDuck {
    @Shadow public abstract void setScreen(@Nullable Screen screen);

    //? if >1.20.6 {
    @Shadow public abstract net.minecraft.client.DeltaTracker getTimer();
    //?} else {
    /*@Shadow public abstract float getDeltaFrameTime();
    *///?}

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
        ((MouseMinecraftCallNotifier) mouseHandler).imFromMinecraftSetScreen();
    }

    /**
     * Without this, the mouse would be left in the middle of the
     * screen, hovering over whatever is there which would look wrong
     * as there is a focus as well.
     */
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(Lnet/minecraft/client/Minecraft;II)V", shift = At.Shift.AFTER))
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

    /*? if >1.20.4 {*/
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;handleAccumulatedMovement()V"))
    /*?} else {*/
    /*@Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"))
    *//*?}*/
    private void doPlayerLook(boolean tick, CallbackInfo ci) {
        Controlify.instance().inGameInputHandler().ifPresent(ih -> ih.processPlayerLook(getTickDelta()));
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/telemetry/ClientTelemetryManager;close()V"))
    private void onMinecraftClose(CallbackInfo ci) {
        Controlify.instance().getControllerManager().ifPresent(ControllerManager::close);
    }

    @Inject(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    /*? if >1.20.6 {*/
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"
                    /*?} else {*/
                    /*target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"
                    *//*?}*/
            )
    )
    private void tickAnimator(boolean tick, CallbackInfo ci) {
        Animator.INSTANCE.tick(getTickDelta());
    }

    /*? if >1.20.1 {*/
    @Inject(method = "addInitialScreens", at = @At("TAIL"))
    private void injectCustomInitialScreens(List<Function<Runnable, Screen>> output, CallbackInfo ci) {
        output.addAll(initialScreenCallbacks);
        initialScreensHappened = true;
    }
    /*?}*/

    @Unique
    private float getTickDelta() {
        /*? if >1.20.6 {*/
        return getTimer().getGameTimeDeltaTicks();
        /*?} else {*/
        /*return getDeltaFrameTime();
        *//*?}*/
    }

    @Override
    public void controlify$registerInitialScreen(Function<Runnable, Screen> screenFactory) {
        boolean doNow = initialScreensHappened;
        /*? if <=1.20.1 {*/
        /*doNow = true;
        *//*?}*/

        if (doNow) {
            Screen lastScreen = this.screen;
            setScreen(screenFactory.apply(() -> setScreen(lastScreen)));
        } else {
            initialScreenCallbacks.add(screenFactory);
        }
    }
}
