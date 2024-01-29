package dev.isxander.controlify.mixins.feature.handheld.autokeybard;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "added", at = @At("HEAD"))
    protected void openOnScreenKeyboard(CallbackInfo ci) {

    }

    @Inject(method = "removed", at = @At("HEAD"))
    protected void closeOnScreenKeyboard(CallbackInfo ci) {

    }
}
