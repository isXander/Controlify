/*? if sodium {*/
/*package dev.isxander.controlify.compatibility.sodium.mixins;

import dev.isxander.controlify.compatibility.sodium.screenop.CycleControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

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
*//*?}*/
