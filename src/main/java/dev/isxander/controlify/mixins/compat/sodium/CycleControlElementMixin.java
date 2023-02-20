package dev.isxander.controlify.mixins.compat.sodium;

import dev.isxander.controlify.compatibility.sodium.CycleControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl$CyclingControlElement")
public abstract class CycleControlElementMixin<T extends Enum<T>> extends ControlElementMixin<T> implements ComponentProcessorProvider {
    @Shadow private int currentIndex;
    @Final @Shadow private T[] allowedValues;

    @Unique private final ComponentProcessor controlify$componentProcessor
            = new CycleControlProcessor(this::cycle);

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$componentProcessor;
    }

    private void cycle(boolean backwards) {
        if (backwards) {
            this.currentIndex = (this.currentIndex - 1 + this.allowedValues.length) % this.allowedValues.length;
        } else {
            this.currentIndex = (this.option.getValue().ordinal() + 1) % this.allowedValues.length;
        }
        this.option.setValue(this.allowedValues[this.currentIndex]);
        this.playClickSound();
    }
}
