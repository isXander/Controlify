package dev.isxander.controlify.mixins.feature.screenop;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.keyboard.KeyboardOverlayScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        //? if >=26.2 {
        net.minecraft.client.gui.Gui.class
        //?} else {
        /*net.minecraft.client.Minecraft.class
        *///?}
)
public class GuiMixin {
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateTitle()V"))
    private void changeScreen(Screen screen, CallbackInfo ci) {
        ComponentProcessorProvider.REGISTRY.clearCache();
    }

    @WrapWithCondition(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
    private boolean preventRemovingOldScreen(Screen oldScreen, @Local(argsOnly = true, name = "screen") Screen screen) {
        return !(screen instanceof KeyboardOverlayScreen);
    }
}
