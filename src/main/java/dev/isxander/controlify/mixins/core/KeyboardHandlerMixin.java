package dev.isxander.controlify.mixins.core;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    // method_22678 is lambda for GLFW keypress hook - do it outside of the `keyPress` method due to fake inputs
    @Inject(method = "method_22678", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;keyPress(JIIII)V"))
    private void onKeyboardInput(long window, int i, int j, int k, int m, CallbackInfo ci) {
        if (window == minecraft.getWindow().getWindow() && Controlify.instance().currentInputMode() != InputMode.MIXED)
            Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
    }

    // method_22677 is lambda for GLFW char input hook - do it outside of the `charTyped` method due to fake inputs
    @Inject(method = "method_22677", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;charTyped(JII)V"))
    private void onCharInput(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (window == minecraft.getWindow().getWindow() && Controlify.instance().currentInputMode() != InputMode.MIXED)
            Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
    }
}
