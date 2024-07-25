package dev.isxander.controlify.mixins.feature.screenkeyboard;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.screenkeyboard.ChatKeyboardDucky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow
    public abstract double getScale();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Definition(id = "floor", method = "Lnet/minecraft/util/Mth;floor(F)I")
    @Expression("floor((float) (@(?) - @(40)) / ?)")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyChatOffset(int y) {
        if (minecraft.screen instanceof ChatScreen chat)
            return (int) (y * (1 - ChatKeyboardDucky.getKeyboardShiftAmount(chat)));
        return y;
    }

    @ModifyExpressionValue(method = "screenToChatY", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getGuiScaledHeight()I"))
    private int modifyScreenY(int original) {
        if (minecraft.screen instanceof ChatScreen chat) {
            float shiftAmount = 1 - ChatKeyboardDucky.getKeyboardShiftAmount(chat);
            return (int) (original * shiftAmount + 40 * shiftAmount); // re-add 40 since it is subtracked in the method
        }
        return original;
    }
}
