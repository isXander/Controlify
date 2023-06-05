package dev.isxander.controlify.mixins.compat.yacl;

import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.yacl.SliderControllerElementComponentProcessor;
import dev.isxander.yacl3.gui.controllers.slider.SliderControllerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SliderControllerElement.class)
public class SliderControllerElementMixin implements ComponentProcessorProvider {
    @Unique private final SliderControllerElementComponentProcessor controlify$processor
            = new SliderControllerElementComponentProcessor((SliderControllerElement) (Object) this);

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$processor;
    }
}
