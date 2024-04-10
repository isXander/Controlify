package dev.isxander.controlify.mixins.feature.guide.ingame;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    /*?if >1.20.4 {*/
    @Shadow @Final
    private net.minecraft.client.gui.LayeredDraw layers;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addButtonGuideLayer(Minecraft minecraft, CallbackInfo ci) {
        layers.add(this::renderButtonGuideIfPresent);
    }
    /*? } else { *//*
    @Inject(method = "render", at = @At(value = "CONSTANT", args = "stringValue=chat"))
    private void renderButtonGuide(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
        renderButtonGuideIfPresent(graphics, tickDelta);
    }
    *//*?}*/

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void tickButtonGuide(CallbackInfo ci) {
        if (minecraft.level == null) return;

        Controlify.instance().inGameButtonGuide().ifPresent(InGameButtonGuide::tick);
    }

    @Unique
    private void renderButtonGuideIfPresent(GuiGraphics graphics, float tickDelta) {
        Controlify.instance().inGameButtonGuide().ifPresent(guide -> {
            minecraft.getProfiler().push("controlify_button_guide");
            guide.renderHud(graphics, tickDelta);
            minecraft.getProfiler().pop();
        });
    }
}
