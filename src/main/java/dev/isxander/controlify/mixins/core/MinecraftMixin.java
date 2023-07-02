package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.ControllerManager;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.utils.Animator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.jetbrains.annotations.Nullable;
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

    @Unique private boolean initNextTick = false;

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

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"))
    private void doPlayerLook(boolean tick, CallbackInfo ci) {
        Controlify.instance().inGameInputHandler().ifPresent(ih -> ih.processPlayerLook(getDeltaFrameTime()));
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/telemetry/ClientTelemetryManager;close()V"))
    private void onMinecraftClose(CallbackInfo ci) {
        ControllerManager.getConnectedControllers().forEach(Controller::close);
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"))
    private void tickAnimator(boolean tick, CallbackInfo ci) {
        Animator.INSTANCE.progress(this.getDeltaFrameTime());
    }
}
