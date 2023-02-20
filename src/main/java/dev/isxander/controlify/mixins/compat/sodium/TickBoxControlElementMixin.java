package dev.isxander.controlify.mixins.compat.sodium;

import dev.isxander.controlify.compatibility.sodium.TickBoxControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl$TickBoxControlElement")
public abstract class TickBoxControlElementMixin extends ControlElementMixin<Boolean> implements ComponentProcessorProvider {
    @Unique private final ComponentProcessor controlify$componentProcessor
            = new TickBoxControlProcessor(this::toggle);

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$componentProcessor;
    }

    private void toggle() {
        this.option.setValue(!this.option.getValue());
        this.playClickSound();
    }
}
