package dev.isxander.controlify.mixins.core;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setLastInputType(Lnet/minecraft/client/InputType;)V"))
    private void onMouseClickInput(long window, int button, int action, int modifiers, CallbackInfo ci) {
        Controlify.getInstance().setCurrentInputMode(InputMode.KEYBOARD_MOUSE);
    }

    @Inject(method = "onMove", at = @At("RETURN"))
    private void onMouseMoveInput(long window, double x, double y, CallbackInfo ci) {
        Controlify.getInstance().setCurrentInputMode(InputMode.KEYBOARD_MOUSE);
    }

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;"))
    private void onMouseScrollInput(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci) {
        Controlify.getInstance().setCurrentInputMode(InputMode.KEYBOARD_MOUSE);
    }
}
