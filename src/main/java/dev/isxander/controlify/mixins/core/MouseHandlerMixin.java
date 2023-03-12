package dev.isxander.controlify.mixins.core;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    // m_sljgmtqm is lambda for GLFW mouse click hook - do it outside of the `onPress` method due to fake inputs
    @Inject(method = "method_22686", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onPress(JIII)V"))
    private void onMouseClickInput(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (window == minecraft.getWindow().getWindow())
            Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
    }

    // m_swhlgdws is lambda for GLFW mouse move hook - do it outside of the `onMove` method due to fake inputs
    @Inject(method = "method_22689", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onMove(JDD)V"))
    private void onMouseMoveInput(long window, double x, double y, CallbackInfo ci) {
        if (window == minecraft.getWindow().getWindow())
            Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
    }

    // m_qoshpwkl is lambda for GLFW mouse scroll hook - do it outside of the `onScroll` method due to fake inputs
    @Inject(method = "method_22687", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onScroll(JDD)V"))
    private void onMouseScrollInput(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci) {
        if (window == minecraft.getWindow().getWindow())
            Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
    }
}
