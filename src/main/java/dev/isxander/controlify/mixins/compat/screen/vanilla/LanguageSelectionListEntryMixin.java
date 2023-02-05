package dev.isxander.controlify.mixins.compat.screen.vanilla;

import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.vanilla.LanguageSelectionListComponentProcessor;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
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
