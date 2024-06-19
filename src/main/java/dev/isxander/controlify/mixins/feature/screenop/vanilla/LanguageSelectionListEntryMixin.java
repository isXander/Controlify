package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.LanguageSelectionListComponentProcessor;
import net.minecraft.client.gui.screens./*? if >1.20.6 >>*//*options.*/ LanguageSelectScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LanguageSelectScreen.LanguageSelectionList.Entry.class)
public class LanguageSelectionListEntryMixin implements ComponentProcessorProvider {
    @Shadow @Final String code;

    @Unique private LanguageSelectionListComponentProcessor controlify$componentProcessor = null;

    @Override
    public ComponentProcessor componentProcessor() {
        // lazily create the component processor so `code` is defined
        if (controlify$componentProcessor == null)
            controlify$componentProcessor = new LanguageSelectionListComponentProcessor(code);

        return controlify$componentProcessor;
    }
}
