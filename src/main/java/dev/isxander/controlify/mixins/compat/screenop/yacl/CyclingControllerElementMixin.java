package dev.isxander.controlify.mixins.compat.screenop.yacl;

import dev.isxander.controlify.screenop.component.ComponentProcessor;
import dev.isxander.controlify.screenop.component.ComponentProcessorProvider;
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
