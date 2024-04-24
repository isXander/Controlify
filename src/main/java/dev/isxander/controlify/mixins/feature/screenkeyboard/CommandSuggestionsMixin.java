package dev.isxander.controlify.mixins.feature.screenkeyboard;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.screenkeyboard.ChatKeyboardDucky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {
    @Shadow
    @Final
    Minecraft minecraft;

    @ModifyExpressionValue(method = {"renderUsage", "showSuggestions"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/Screen;height:I"))
    private int modifyUsageHeight(int height) {
        if (minecraft.screen instanceof ChatScreen chat && ChatKeyboardDucky.hasKeyboard(chat))
            return height / 2;
        return height;
    }
}
