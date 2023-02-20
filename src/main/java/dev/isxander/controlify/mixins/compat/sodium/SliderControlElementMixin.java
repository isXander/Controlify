package dev.isxander.controlify.mixins.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.compatibility.sodium.SliderControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.options.control.SliderControl$Button")
public abstract class SliderControlElementMixin extends ControlElementMixin<Integer> implements ComponentProcessorProvider {
    @Shadow public abstract int getIntValue();
    @Shadow @Final private int interval;
    @Shadow protected abstract void setValue(double d);
    @Shadow public abstract double getThumbPositionForValue(int value);

    @Unique private final ComponentProcessor controlify$componentProcessor
            = new SliderControlProcessor(this::incrementSlider);

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/gui/options/control/SliderControl$Button;hovered:Z", opcode = Opcodes.GETFIELD))
    private boolean shouldRenderSlider(boolean hovered) {
        return hovered || this.isFocused();
    }

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$componentProcessor;
    }

    private void incrementSlider(boolean reverse) {
        int inc = reverse ? -1 : 1;
        this.setValue(this.getThumbPositionForValue(this.getIntValue() + inc * this.interval));
    }
}
