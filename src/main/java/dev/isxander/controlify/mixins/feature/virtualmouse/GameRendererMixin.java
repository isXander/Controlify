package dev.isxander.controlify.mixins.feature.virtualmouse;

import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.Controlify;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", shift = At.Shift.AFTER))
    private void onPostRenderScreen(float tickDelta, long startTime, boolean tick, CallbackInfo ci, @Local GuiGraphics graphics) {
        Controlify.instance().virtualMouseHandler().renderVirtualMouse(graphics);
    }
}
