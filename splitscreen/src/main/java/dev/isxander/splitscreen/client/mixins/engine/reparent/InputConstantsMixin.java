package dev.isxander.splitscreen.client.mixins.engine.reparent;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.engine.impl.reparenting.wm.NativeWindowHandle;
import dev.isxander.splitscreen.client.engine.impl.reparenting.wm.WindowManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
    /**
     * Don't allow the mouse to be grabbed to a window when we're in splitscreen.
     * Ideally splitscreen should allow one keyboard/mouse player, but we're not there yet.
     */
    @WrapMethod(method = "grabOrReleaseMouse")
    private static void shouldAllowMouseGrab(long window, int cursorValue, double xPos, double yPos, Operation<Void> original) {
        if (SplitscreenBootstrapper.getController()
                .map(c -> c.getLocalPawn().getAssociatedInputMethod().isKeyboardAndMouse())
                .orElse(false) || !SplitscreenBootstrapper.isSplitscreen()) {
            WindowManager windowManager = WindowManager.get();
            NativeWindowHandle nativeWindowHandle = windowManager.getNativeWindowHandle(window);
            windowManager.setWindowFocused(nativeWindowHandle);
            windowManager.setWindowForeground(nativeWindowHandle);

            original.call(window, cursorValue, xPos, yPos);
        }
    }
}
