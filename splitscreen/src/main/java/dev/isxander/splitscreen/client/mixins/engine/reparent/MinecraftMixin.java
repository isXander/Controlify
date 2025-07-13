package dev.isxander.splitscreen.client.mixins.engine.reparent;

import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ReparentingHostSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ReparentingRemoteSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.reparenting.events.VanillaWindowReadyEvent;
import dev.isxander.splitscreen.client.engine.impl.reparenting.parent.ParentWindow;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    /**
     * Hooks in and sets the splitscreen controller's window ready state.
     */
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V"))
    private void onWindowReady(CallbackInfo ci) {
        VanillaWindowReadyEvent.EVENT.invoker().onVanillaWindowReady();
        SplitscreenBootstrapper.getPawn().flatMap(ReparentingRemoteSplitscreenEngine::tryGet).ifPresent(ReparentingRemoteSplitscreenEngine::onWindowInit);
    }

    /**
     * Ensures the parent window is closed when the game exist.
     */
    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;close()V"))
    private void closeParentWindow(CallbackInfo ci) {
        SplitscreenBootstrapper.getController()
                .flatMap(ReparentingHostSplitscreenEngine::tryGet)
                .flatMap(engine -> Optional.ofNullable(engine.getParentWindow()))
                .ifPresent(ParentWindow::close);
    }
}
