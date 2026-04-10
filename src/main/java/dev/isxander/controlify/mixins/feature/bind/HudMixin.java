package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.utils.MinecraftUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        //? if >=26.2 {
        net.minecraft.client.gui.Hud.class
        //?} else {
        /*net.minecraft.client.gui.Gui.class
        *///?}
)
public class HudMixin {

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void shouldRenderCrosshair(CallbackInfo ci) {
        if (MinecraftUtil.getScreen() instanceof RadialMenuScreen) {
            ci.cancel();
        }
    }
}
