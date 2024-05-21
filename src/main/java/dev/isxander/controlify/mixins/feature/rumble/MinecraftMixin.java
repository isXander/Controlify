package dev.isxander.controlify.mixins.feature.rumble;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    /*? if >=1.20.4 {*/
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    /*?} else {*//*
    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    *//*?}*/
    private void clearRumbleEffects(Screen disconnectScreen, CallbackInfo ci) {
        ControlifyApi.get().getCurrentController()
                .flatMap(ControllerEntity::rumble)
                .ifPresent(controller -> controller.rumbleManager().clearEffects());
    }
}
