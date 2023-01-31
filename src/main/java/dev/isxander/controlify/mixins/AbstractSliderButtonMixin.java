package dev.isxander.controlify.mixins;

import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.screen.component.SliderComponentProcessor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin to insert a custom {@link ComponentProcessor} into slider to support left/right movement without navigating to next component.
 */
@Mixin(AbstractSliderButton.class)
public class AbstractSliderButtonMixin implements ComponentProcessorProvider {
    @Shadow private boolean canChangeValue;

    @Unique
    private final SliderComponentProcessor controlify$processor = new SliderComponentProcessor(
            (AbstractSliderButton) (Object) this,
            () -> this.canChangeValue,
            val -> this.canChangeValue = val
    );

    @Override
    public ComponentProcessor<AbstractSliderButton> componentProcessor() {
        return controlify$processor;
    }
}
