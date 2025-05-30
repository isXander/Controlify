package dev.isxander.splitscreen.client.mixins.screenop;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.remote.gui.ImHiddenScreen;
import dev.isxander.splitscreen.client.features.screenop.ScreenSplitscreenModeRegistry;
import dev.isxander.splitscreen.client.features.screenop.ScreenSplitscreenMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    /**
     * Don't show screens when on hidden pawns. This could lead to unintended behaviour.
     * @param newScreen the screen that is attempted to be set
     * @param splitscreenModeRef local ref of the gathered splitscreen mode to pass to the next injector below
     * @return the screen to assign to the Minecraft.screen field
     */
    @Definition(id = "screen", field = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;")
    @Definition(id = "newScreen", local = @Local(type = Screen.class, argsOnly = true))
    @Expression("this.screen = @(newScreen)")
    @ModifyExpressionValue(method = "setScreen", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Screen overrideScreenIfPawnFullscreen(Screen newScreen, @Local(argsOnly = true) LocalRef<Screen> newScreenRef, @Share("splitscreenMode") LocalRef<ScreenSplitscreenMode> splitscreenModeRef) {
        ScreenSplitscreenMode splitscreenMode = ScreenSplitscreenModeRegistry.getMode(newScreen);
        splitscreenModeRef.set(splitscreenMode);
        if (splitscreenMode == ScreenSplitscreenMode.FULLSCREEN && SplitscreenBootstrapper.getPawn().isPresent()) {
            System.out.println("Attempted to set " + newScreen.getClass().getSimpleName() + " as the screen on a pawn client.");

            var hiddenScreen = new ImHiddenScreen();
            newScreenRef.set(hiddenScreen);
            return hiddenScreen;
        }

        return newScreen;
    }

    /**
     * Update whether the current screen is a fullscreen or splitscreen screen
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
    private void updateSplitscreenMode(CallbackInfo ci, @Share("splitscreenMode") LocalRef<ScreenSplitscreenMode> splitscreenModeRef) {
        ScreenSplitscreenMode splitscreenMode = splitscreenModeRef.get();
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            controller.setSplitscreenMode(splitscreenMode);
        });
    }
}
