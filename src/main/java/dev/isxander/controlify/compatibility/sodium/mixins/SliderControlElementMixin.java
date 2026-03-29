//? if sodium {
package dev.isxander.controlify.compatibility.sodium.mixins;

import dev.isxander.controlify.compatibility.sodium.screenop.SliderControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import net.caffeinemc.mods.sodium.client.config.structure.IntegerOption;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.options.control.AbstractOptionList;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl$SliderControlElement")
public abstract class SliderControlElementMixin extends ControlElement implements ComponentProcessorProvider {
    @Shadow @Final private IntegerOption option;

    @Unique private final ComponentProcessor controlify$componentProcessor
            = new SliderControlProcessor(this::controlify$incrementSlider);

    public SliderControlElementMixin(AbstractOptionList list, Dim2i dim, ColorTheme theme) {
        super(list, dim, theme);
    }

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$componentProcessor;
    }

    @Unique
    private void controlify$incrementSlider(boolean reverse) {
        var range = option.getSteppedValidator();
        option.modifyValue(Mth.clamp(
                option.getValidatedValue() + (reverse ? -range.step() : range.step()),
                range.min(), range.max()
        ));
    }
}
//?}
