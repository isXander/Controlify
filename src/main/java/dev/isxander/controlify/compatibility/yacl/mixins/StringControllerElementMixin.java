package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.controlify.compatibility.yacl.screenop.StringControllerElementComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StringControllerElement.class)
public class StringControllerElementMixin implements ComponentProcessorProvider {
    @Unique
    private final ComponentProcessor componentProcessor = new StringControllerElementComponentProcessor(
            (StringControllerElement) (Object) this
    );

    @Override
    public ComponentProcessor componentProcessor() {
        return componentProcessor;
    }
}
