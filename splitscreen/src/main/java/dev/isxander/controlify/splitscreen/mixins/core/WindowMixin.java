package dev.isxander.controlify.splitscreen.mixins.core;

import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import org.lwjgl.glfw.GLFWImage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
    @Shadow @Final private static Logger LOGGER;

    @Unique private boolean hasDoneInitialSetup = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void markInitialSetup(CallbackInfo ci) {
        // Just a bug check to ensure things aren't going wrong preventing window from setting up correctly.
        this.hasDoneInitialSetup = true;
    }

    @ModifyArg(method = "setIcon", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowIcon(JLorg/lwjgl/glfw/GLFWImage$Buffer;)V"))
    private GLFWImage.Buffer propagateWindowIconToParent(GLFWImage.Buffer iconBuffer) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            assert controller.getParentWindow() != null; // ParentWindow is created before the vanilla Window.
            controller.getParentWindow().setIcon(iconBuffer);
        });

        return iconBuffer;
    }

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void propagateTitleToParent(String title, CallbackInfo ci) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            assert controller.getParentWindow() != null; // ParentWindow is created before the vanilla Window.
            controller.getParentWindow().setTitle(title);
        });
    }

    @Inject(
            method = {
                    "setMode",
                    "changeFullscreenVideoMode",
                    "setPreferredFullscreenVideoMode",
                    "toggleFullScreen",
                    "setWindowed",
                    "updateFullscreen"
            },
            at = @At("HEAD"),
            cancellable = true)
    private void preventModeChange(CallbackInfo ci) {
        preventIfSplitscreen("Controlify splitscreen prevented window mode change", ci);
    }

    @Unique
    private void preventIfSplitscreen(String message, CallbackInfo ci) {
        if (SplitscreenBootstrapper.isSplitscreen()) {
            if (!hasDoneInitialSetup) {
                return;
            }

            LOGGER.info(message);
            ci.cancel();
        }
    }
}
