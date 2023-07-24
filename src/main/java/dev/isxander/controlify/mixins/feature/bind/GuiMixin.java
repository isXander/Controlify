package dev.isxander.controlify.mixins.feature.bind;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private boolean shouldRenderCrosshair(Gui instance, GuiGraphics graphics) {
        return !(minecraft.screen instanceof RadialMenuScreen);
    }
}
