package dev.isxander.controlify.mixins;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setLastInputType(Lnet/minecraft/client/InputType;)V"))
    private void onKeyboardInput(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        Controlify.getInstance().setCurrentInputMode(InputMode.KEYBOARD_MOUSE);
    }
}
