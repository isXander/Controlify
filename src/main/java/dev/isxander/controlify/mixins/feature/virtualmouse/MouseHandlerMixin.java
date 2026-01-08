package dev.isxander.controlify.mixins.feature.virtualmouse;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @WrapWithCondition(
            method = "releaseMouse",
            at = @At(
                    value = "INVOKE",
                    //? if >=1.21.9 {
                    target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"
                    //?} else {
                    /*target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"
                    *///?}
            )
    )
    private boolean shouldReleaseMouse(
            //? if >=1.21.9 {
            Window window, int cursor, double x, double y
            //?} else {
            /*long window, int cursor, double x, double y
            *///?}
    ) {
        // mouse cursor appears for a split second when going into guis on impl input
        return Controlify.instance().currentInputMode() != InputMode.CONTROLLER;
    }
}
