package dev.isxander.controlify.mixins.feature.screenop.yacl;

import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.compat.yacl.CyclingControllerElementComponentProcessor;
import dev.isxander.yacl.gui.controllers.cycling.CyclingControllerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CyclingControllerElement.class)
public class CyclingControllerElementMixin implements ComponentProcessorProvider {
    @Unique private final CyclingControllerElementComponentProcessor controlify$processor
            = new CyclingControllerElementComponentProcessor((CyclingControllerElement) (Object) this);

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$processor;
    }
}
