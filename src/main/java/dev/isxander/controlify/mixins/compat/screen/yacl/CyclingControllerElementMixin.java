package dev.isxander.controlify.mixins.compat.screen.yacl;

import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.yacl.CyclingControllerElementComponentProcessor;
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
