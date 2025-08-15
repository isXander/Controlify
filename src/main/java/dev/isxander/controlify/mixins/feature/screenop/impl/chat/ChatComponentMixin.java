package dev.isxander.controlify.mixins.feature.screenop.impl.chat;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.screenop.keyboard.ChatKeyboardDucky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow
    public abstract double getScale();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique private static final int VANILLA_CHAT_PADDING = 40;
    @Unique private static final int SHIFTED_CHAT_PADDING = 20;

    @Definition(id = "floor", method = "Lnet/minecraft/util/Mth;floor(F)I")
    @Expression("floor((float) (@(?) - 40) / ?)")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyChatOffset(int y) {
        if (minecraft.screen instanceof ChatScreen chat)
            return (int) (y * (1 - ChatKeyboardDucky.getKeyboardShiftAmount(chat)));
        return y;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "intValue=" + VANILLA_CHAT_PADDING))
    private int modifyChatToInputPadding(int padding) {
        if (minecraft.screen instanceof ChatScreen chat) {
            if (ChatKeyboardDucky.getKeyboardShiftAmount(chat) > 0) {
                return SHIFTED_CHAT_PADDING;
            }
        }
        return padding;
    }

    @ModifyVariable(method = "screenToChatY", at = @At("HEAD"), argsOnly = true)
    private double modifyScreenY(double y) {
        if (minecraft.screen instanceof ChatScreen chat) {
            float shiftAmount = ChatKeyboardDucky.getKeyboardShiftAmount(chat);
            if (shiftAmount > 0) {
                double shiftPixels = shiftAmount * minecraft.getWindow().getGuiScaledHeight() - (VANILLA_CHAT_PADDING - SHIFTED_CHAT_PADDING);
                return y + shiftPixels;
            }
        }
        return y;
    }
}
