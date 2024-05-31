/*? if sodium {*/
/*package dev.isxander.controlify.compatibility.sodium.mixins;

import dev.isxander.controlify.compatibility.sodium.screenop.TickBoxControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl$TickBoxControlElement", remap = false)
public abstract class TickBoxControlElementMixin implements ComponentProcessorProvider {
    @Shadow public abstract void toggleControl();

    @Unique private final ComponentProcessor controlify$componentProcessor
            = new TickBoxControlProcessor(this::toggleControl);

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$componentProcessor;
    }
}
*//*?}*/
