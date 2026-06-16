package dev.isxander.controlify.mixins.feature.virtualmouse;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(
        //? if >=26.2 {
        net.minecraft.client.gui.Gui.class
        //?} else {
        /*net.minecraft.client.Minecraft.class
        *///?}
)
public class GuiMixin {
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateTitle()V"))
    private void onScreenChanged(Screen screen, CallbackInfo ci) {
        Optional.ofNullable(Controlify.instance().virtualMouseHandler())
                .ifPresent(VirtualMouseHandler::onScreenChanged);
    }
}
