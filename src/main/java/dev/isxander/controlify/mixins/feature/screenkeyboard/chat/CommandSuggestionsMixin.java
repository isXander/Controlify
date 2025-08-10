package dev.isxander.controlify.mixins.feature.screenkeyboard.chat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.screenkeyboard.ChatKeyboardDucky;
import dev.isxander.controlify.screenop.compat.vanilla.ChatScreenProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin implements ChatScreenProcessor.CmdSuggestionsController {
    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    @Nullable
    private CommandSuggestions.@Nullable SuggestionsList suggestions;

    @ModifyExpressionValue(method = {"renderUsage", "showSuggestions"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/Screen;height:I"))
    private int modifyUsageHeight(int height) {
        if (minecraft.screen instanceof ChatScreen chat)
            return (int) (height * (1 - ChatKeyboardDucky.getKeyboardShiftAmount(chat)));
        return height;
    }

    @Override
    public boolean controlify$cycle(int amount) {
        if (this.suggestions == null) return false;

        this.suggestions.cycle(amount);
        return true;
    }

    @Override
    public boolean controlify$useSuggestion() {
        if (this.suggestions == null) return false;

        this.suggestions.useSuggestion();
        return true;
    }

    @Override
    public boolean controlify$hasAvailableSuggestions() {
        return this.suggestions != null;
    }
}
