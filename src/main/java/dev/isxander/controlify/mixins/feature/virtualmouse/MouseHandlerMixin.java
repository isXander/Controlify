package dev.isxander.controlify.mixins.feature.virtualmouse;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @WrapWithCondition(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"))
    private boolean shouldReleaseMouse(long window, int newMouseState, double x, double y) {
        // mouse cursor appears for a split second when going into guis on controller input
        return Controlify.instance().currentInputMode() != InputMode.CONTROLLER;
    }
}
