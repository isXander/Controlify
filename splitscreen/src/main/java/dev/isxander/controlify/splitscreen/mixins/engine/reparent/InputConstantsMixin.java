package dev.isxander.controlify.splitscreen.mixins.engine.reparent;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
    /**
     * Don't allow the mouse to be grabbed to a window when we're in splitscreen.
     * Ideally splitscreen should allow one keyboard/mouse player, but we're not there yet.
     */
    @WrapMethod(method = "grabOrReleaseMouse")
    private static void shouldAllowMouseGrab(long window, int cursorValue, double xPos, double yPos, Operation<Void> original) {
        if (!SplitscreenBootstrapper.isSplitscreen()) {
            original.call(window, cursorValue, xPos, yPos);
        }
    }
}
