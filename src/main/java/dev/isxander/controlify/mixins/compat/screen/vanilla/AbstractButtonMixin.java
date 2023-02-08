package dev.isxander.controlify.mixins.compat.screen.vanilla;

import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.vanilla.AbstractButtonComponentProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractButton.class)
public class AbstractButtonMixin implements ComponentProcessorProvider {
    @Unique private final AbstractButtonComponentProcessor controlify$processor
            = new AbstractButtonComponentProcessor((AbstractButton) (Object) this);

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$processor;
    }
}
