package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.gui.screen.BetaNoticeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow public abstract float getDeltaFrameTime();

    @Shadow public abstract ToastComponent getToasts();

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private ReloadInstance onInputInitialized(ReloadInstance resourceReload) {
        // Controllers need to be initialized extremely late due to the data-driven nature of controllers.
        resourceReload.done().thenRun(() -> Controlify.instance().initializeControlify());
        return resourceReload;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;setup(J)V", shift = At.Shift.AFTER))
    private void onInputInitialized(CallbackInfo ci) {
        Controlify.instance().preInitialiseControlify();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"))
    private void doPlayerLook(boolean tick, CallbackInfo ci) {
        Controlify.instance().inGameInputHandler().processPlayerLook(getDeltaFrameTime());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void showBetaScreen(GameConfig args, CallbackInfo ci) {
        if (Controlify.instance().config().isFirstLaunch())
            setScreen(new BetaNoticeScreen());
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private ReloadInstance onReloadResources(ReloadInstance resourceReload) {
        resourceReload.done().thenRun(() -> {
            if (Controlify.instance().controllerHIDService().isDisabled()) {
                getToasts().addToast(SystemToast.multiline((Minecraft) (Object) this, SystemToast.SystemToastIds.UNSECURE_SERVER_WARNING, Component.translatable("controlify.error.hid"), Component.translatable("controlify.error.hid.desc")));
            }
        });
        return resourceReload;
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/telemetry/ClientTelemetryManager;close()V"))
    private void onMinecraftClose(CallbackInfo ci) {
        Controller.CONTROLLERS.values().forEach(Controller::close);
    }
}
