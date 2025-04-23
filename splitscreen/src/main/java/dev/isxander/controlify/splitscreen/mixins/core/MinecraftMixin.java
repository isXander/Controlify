package dev.isxander.controlify.splitscreen.mixins.core;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.window.ParentWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VirtualScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    /**
     * Hooks in to create a parent window for the splitscreen controller.
     * @param instance receiver type
     * @param screenSize arg1
     * @param videoModeName arg2
     * @param title arg3
     * @param original operation
     * @return client window
     */
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/VirtualScreen;newWindow(Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;"))
    private Window createParentWindow(VirtualScreen instance, DisplayData screenSize, String videoModeName, String title, Operation<Window> original) {
        SplitscreenBootstrapper.getController().ifPresent(controller ->
                controller.setupParentWindow(screenSize, ((VirtualScreenAccessor) (Object) instance).getScreenManager(), title));

        // create original client window
        return original.call(instance, screenSize, videoModeName, title);
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
    private boolean markWindowReady(Minecraft instance) {
        return SplitscreenBootstrapper.getController().map(controller -> {
            controller.markWindowReady();
            return false;
        }).orElse(true);
    }

    /**
     * Ensures the parent window is closed when the game exist.
     */
    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;close()V", shift = At.Shift.AFTER))
    private void closeParentWindow(CallbackInfo ci) {
        SplitscreenBootstrapper.getController()
                .flatMap(controller -> Optional.ofNullable(controller.getParentWindow()))
                .ifPresent(ParentWindow::close);
    }
}
