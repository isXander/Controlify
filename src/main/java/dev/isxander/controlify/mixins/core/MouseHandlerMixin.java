package dev.isxander.controlify.mixins.core;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.utils.MouseMinecraftCallNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin implements MouseMinecraftCallNotifier {
    @Shadow @Final private Minecraft minecraft;

    @Unique private boolean calledFromMinecraftSetScreen = false;

    // method_22686 is lambda for GLFW mouse click hook - do it outside of the `onPress` method due to fake inputs
    @Inject(method = "method_22686", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onPress(JIII)V"))
    private void onMouseClickInput(long window, int button, int action, int modifiers, CallbackInfo ci) {
        onMouse(window);
    }

    // method_22689 is lambda for GLFW mouse move hook - do it outside of the `onMove` method due to fake inputs
    @Inject(method = "method_22689", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onMove(JDD)V"))
    private void onMouseMoveInput(long window, double x, double y, CallbackInfo ci) {
        onMouse(window);
    }

    // method_22687 is lambda for GLFW mouse scroll hook - do it outside of the `onScroll` method due to fake inputs
    @Inject(method = "method_22687", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onScroll(JDD)V"))
    private void onMouseScrollInput(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci) {
        onMouse(window);
    }

    @Unique
    private void onMouse(long window) {
        if (window == minecraft.getWindow().getWindow()) {
            if (Controlify.instance().currentInputMode() != InputMode.MIXED) {
                Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
            } else {
                Controlify.instance().showCursorTemporarily();
            }
        }
    }

    /**
     * Without this, mouse is left in the center of the screen that conflicts with controller focus.
     */
    @Inject(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"))
    private void moveMouseIfNecessary(CallbackInfo ci) {
        if (!calledFromMinecraftSetScreen && ControlifyApi.get().currentInputMode().isController()) {
            Controlify.instance().hideMouse(true, true);
        }
    }

    // shift after RETURN to escape the if statement scope
    @Inject(method = "releaseMouse", at = @At(value = "RETURN"))
    private void resetCalledFromMinecraftSetScreen(CallbackInfo ci) {
        calledFromMinecraftSetScreen = false;
    }

    @Override
    public void imFromMinecraftSetScreen() {
        calledFromMinecraftSetScreen = true;
    }
}
