package dev.isxander.controlify.splitscreen.mixins.core;

import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
                throw new IllegalStateException("Window did not get enough time to do initial setup before Splitscreen was setup.");
            }

            LOGGER.info(message);
            ci.cancel();
        }
    }
}
