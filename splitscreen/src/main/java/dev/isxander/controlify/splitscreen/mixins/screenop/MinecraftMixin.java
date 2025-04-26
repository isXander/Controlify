package dev.isxander.controlify.splitscreen.mixins.screenop;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.remote.screenop.ImHiddenScreen;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    /**
     * Hooks in when the screen is set to update the splitscreen mode.
     * @param guiScreenRef arg from target method, the new screen to be set - has been modified by the method
     */
    @Inject(
            method = "setScreen",
            at = {
                    // if screen is not null, then hook AFTER init
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(Lnet/minecraft/client/Minecraft;II)V", shift = At.Shift.AFTER),
                    // if screen is null, hook first invoke in the else block
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;resume()V")
            }
    )
    private void updateSplitscreenMode(CallbackInfo ci, @Local(argsOnly = true) LocalRef<Screen> guiScreenRef) {
        Screen guiScreen = guiScreenRef.get();

        ScreenSplitscreenMode splitscreenMode = ScreenSplitscreenBehaviour.getModeForScreen(guiScreen);

        if (splitscreenMode == ScreenSplitscreenMode.FULLSCREEN && SplitscreenBootstrapper.getPawn().isPresent()) {
            guiScreenRef.set(new ImHiddenScreen());
        } else {
            SplitscreenBootstrapper.getController().ifPresent(controller -> {
                controller.setSplitscreenMode(splitscreenMode);
            });
        }
    }
}
