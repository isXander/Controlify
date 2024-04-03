package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.utils.MouseMinecraftCallNotifier;
import dev.isxander.controlify.utils.animation.impl.Animator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow public abstract void setScreen(@Nullable Screen screen);
    @Shadow public abstract float getDeltaFrameTime();

    @Shadow @Final public MouseHandler mouseHandler;
    @Unique private boolean initNextTick = false;

    // Ideally, this would be done in MouseHandler#releaseMouse, but moving
    // the mouse before the screen init is bad, because some mods (e.g. PuzzleLib)
    // have custom mouse events that call into screens, events that have not been
    // initialised yet in Screen#init. Causing NPEs and many strange issues.
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;releaseMouse()V", shift = At.Shift.BEFORE))
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

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private ReloadInstance onInputInitialized(ReloadInstance resourceReload) {
        // Controllers need to be initialized extremely late due to the data-driven nature of controllers.
        // We need to bypass thenRun because any runnable is ran inside of a `minecraft.execute()`, which suppresses exceptions
        resourceReload.done().thenRun(() -> initNextTick = true);
        return resourceReload;
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runAllTasks()V"))
    private void initControlifyNow(boolean tick, CallbackInfo ci) {
        if (initNextTick) {
            Controlify.instance().initializeControlify();
            initNextTick = false;
        }
    }

    /*? if >1.20.4 {*/
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;handleAccumulatedMovement()V"))
    /*? } else {*//*
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"))
    *//*?}*/
    private void doPlayerLook(boolean tick, CallbackInfo ci) {
        Controlify.instance().inGameInputHandler().ifPresent(ih -> ih.processPlayerLook(getDeltaFrameTime()));
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/telemetry/ClientTelemetryManager;close()V"))
    private void onMinecraftClose(CallbackInfo ci) {
        Controlify.instance().getControllerManager().ifPresent(ControllerManager::close);
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"))
    private void tickAnimator(boolean tick, CallbackInfo ci) {
        Animator.INSTANCE.tick(this.getDeltaFrameTime());
    }
}
