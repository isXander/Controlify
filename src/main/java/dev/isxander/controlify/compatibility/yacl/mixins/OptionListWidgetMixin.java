package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.controlify.compatibility.yacl.screenop.OptionListWidgetComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.yacl3.gui.OptionListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OptionListWidget.class)
public class OptionListWidgetMixin implements ComponentProcessorProvider {
    @Unique private final OptionListWidgetComponentProcessor controlify$processor =
            new OptionListWidgetComponentProcessor((OptionListWidget) (Object) this);

    @Override
    public ComponentProcessor componentProcessor() {
        return this.controlify$processor;
    }
}
