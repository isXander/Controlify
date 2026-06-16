package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.ingame.InGameInputHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
        //? if >=26.2 {
        net.minecraft.client.gui.Hud.class
        //?} else {
        /*net.minecraft.client.gui.Gui.class
        *///?}
)
public class HudMixin {
    @ModifyExpressionValue(method = "extractTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean shouldShowPlayerList(boolean keyDown) {
        return keyDown || Controlify.instance().inGameInputHandler().map(InGameInputHandler::shouldShowPlayerList).orElse(false);
    }
}
