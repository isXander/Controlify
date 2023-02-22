package dev.isxander.controlify.mixins.compat.sodium;

import dev.isxander.controlify.compatibility.sodium.CycleControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl$CyclingControlElement", remap = false)
public abstract class CycleControlElementMixin implements ComponentProcessorProvider {
    @Shadow public abstract void cycleControl(boolean reverse);

    @Unique private final ComponentProcessor controlify$componentProcessor
            = new CycleControlProcessor(this::cycleControl);

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$componentProcessor;
    }
}
