package dev.isxander.splitscreen.client.mixins.engine.reparent;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.platform.DisplayData;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ReparentingHostSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ReparentingRemoteSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.reparenting.parent.ParentWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VirtualScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private VirtualScreen virtualScreen;

    @Shadow
    protected abstract String createTitle();

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/VirtualScreen;newWindow(Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;"))
    private DisplayData captureDisplayData(DisplayData displayData, @Share("screenSize") LocalRef<DisplayData> screenSize) {
        screenSize.set(displayData);
        return displayData;
    }

    /**
     * Hooks in and sets the splitscreen controller's window ready state.
     * <p>
     * This is conditional on the resizeDisplay because markWindowReady will cause resizeDisplay to be called anyway.
     * Allow vanilla call to continue if splitscreen controller is not present.
     * @param instance receiver
     * @return if vanilla should call resizeDisplay
     */
    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V"))
    private boolean markWindowReady(Minecraft instance, @Share("screenSize") LocalRef<DisplayData> screenSize) {
        SplitscreenBootstrapper.getPawn().flatMap(ReparentingRemoteSplitscreenEngine::tryGet).ifPresent(ReparentingRemoteSplitscreenEngine::onWindowInit);

        return SplitscreenBootstrapper.getController().flatMap(ReparentingHostSplitscreenEngine::tryGet).map(engine -> {
            engine.initWindow(screenSize.get(), ((VirtualScreenAccessor) (Object) virtualScreen).getScreenManager(), this.createTitle());
            return false;
        }).orElse(true);
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
