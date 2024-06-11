package dev.isxander.controlify.mixins.feature.virtualmouse;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateTitle()V"))
    private void onScreenChanged(Screen screen, CallbackInfo ci) {
        Optional.ofNullable(Controlify.instance().virtualMouseHandler())
                .ifPresent(VirtualMouseHandler::onScreenChanged);
    }

    /*? if >1.20.4 {*/
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;handleAccumulatedMovement()V"))
    /*?} else {*/
    /*@Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"))
    *//*?}*/
    private void onUpdateMouse(boolean tick, CallbackInfo ci) {
        Optional.ofNullable(Controlify.instance().virtualMouseHandler())
                .ifPresent(VirtualMouseHandler::updateMouse);
    }
}
