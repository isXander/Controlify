package dev.isxander.controlify.mixins.compat.screen.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.vanilla.SliderComponentProcessor;
import net.minecraft.client.InputType;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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

    @ModifyExpressionValue(method = "setFocused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getLastInputType()Lnet/minecraft/client/InputType;"))
    private InputType shouldChangeValue(InputType type) {
        if (Controlify.instance().currentInputMode() == InputMode.CONTROLLER)
            return InputType.NONE; // none doesn't pass condition
        return type;
    }

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$processor;
    }
}
