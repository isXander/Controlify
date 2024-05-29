/*? if sodium {*/
package dev.isxander.controlify.compatibility.sodium.mixins;

import dev.isxander.controlify.compatibility.sodium.screenop.SliderControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlElement;
import me.jellysquid.mods.sodium.client.util.Dim2i;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;

@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.options.control.SliderControl$Button", remap = false)
public abstract class SliderControlElementMixin extends ControlElement<Integer> implements ComponentProcessorProvider {
    @Shadow @Final private int interval;
    @Shadow @Final private int min;
    @Shadow @Final private int max;

    @Unique private final ComponentProcessor controlify$componentProcessor
            = new SliderControlProcessor(this::incrementSlider);

    public SliderControlElementMixin(Option<Integer> option, Dim2i dim) {
        super(option, dim);
    }

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$componentProcessor;
    }

    private void incrementSlider(boolean reverse) {
        this.option.setValue(Mth.clamp(this.option.getValue() + (reverse ? -this.interval : this.interval), this.min, this.max));
    }
}
/*?}*/
