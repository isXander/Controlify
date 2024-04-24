package dev.isxander.controlify.mixins.feature.screenkeyboard;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.screenkeyboard.ChatKeyboardDucky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow
    public abstract double getScale();

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(F)I", ordinal = 0))
    private float modifyBottomY(float bottomY, @Local(argsOnly = true) GuiGraphics graphics) {
        if (minecraft.screen instanceof ChatScreen chat && ChatKeyboardDucky.hasKeyboard(chat))
            return (float)((graphics.guiHeight() / 2f - 12 - 8) / getScale());
        return bottomY;
    }

    @ModifyExpressionValue(method = "screenToChatY", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getGuiScaledHeight()I"))
    private int modifyScreenY(int original) {
        if (minecraft.screen instanceof ChatScreen chat && ChatKeyboardDucky.hasKeyboard(chat))
            return (int) (original / 2f + 40 - 12 - 8); // re-add 40 since it is subtracked in the method
        return original;
    }
}
