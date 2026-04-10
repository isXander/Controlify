package dev.isxander.controlify.mixins.feature.screenop.impl.chat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.screenop.keyboard.ChatKeyboardDucky;
import dev.isxander.controlify.screenop.compat.vanilla.ChatScreenProcessor;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
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

    @ModifyExpressionValue(
            method = {
                    //? if >=26.2 {
                    "extractUsage",
                    //?} else {
                    /*"renderUsage",
                    *///?}
                    "showSuggestions"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screens/Screen;height:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int modifyUsageHeight(int height) {
        if (MinecraftUtil.getScreen() instanceof ChatScreen chat)
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
