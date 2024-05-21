package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void shouldRenderCrosshair(
            GuiGraphics guiGraphics,
            /*? if >1.20.4 {*/float delta,/*?}*/
            CallbackInfo ci
    ) {
        if (minecraft.screen instanceof RadialMenuScreen) {
            ci.cancel();
        }
    }
}
