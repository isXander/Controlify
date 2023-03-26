package dev.isxander.controlify.mixins.feature.guide.ingame;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow private int screenWidth;
    @Shadow private int screenHeight;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "CONSTANT", args = "stringValue=chat"))
    private void renderButtonGuide(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        if (Controlify.instance().inGameButtonGuide() != null) {
            minecraft.getProfiler().push("controlify_button_guide");
            Controlify.instance().inGameButtonGuide().renderHud(matrices, tickDelta, screenWidth, screenHeight);
            minecraft.getProfiler().pop();
        }
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void tickButtonGuide(CallbackInfo ci) {
        if (Controlify.instance().inGameButtonGuide() != null) {
            Controlify.instance().inGameButtonGuide().tick();
        }
    }
}
