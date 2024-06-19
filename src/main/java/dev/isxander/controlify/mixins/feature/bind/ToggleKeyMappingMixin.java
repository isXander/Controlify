package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.bindings.KeyMappingHandle;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BooleanSupplier;

@Mixin(ToggleKeyMapping.class)
public abstract class ToggleKeyMappingMixin extends KeyMappingMixin implements KeyMappingHandle {

    @Shadow
    @Final @Mutable
    private BooleanSupplier needsToggle;

    @Shadow
    public abstract void setDown(boolean value);

    @Override
    public void controlify$setPressed(boolean isDown) {
        if (isDown)
            this.incClickCount();
        this.setDown(isDown);
    }

    @Override
    public void controlify$addToggleCondition(BooleanSupplier condition) {
        BooleanSupplier oldCondition = needsToggle;
        needsToggle = () -> oldCondition.getAsBoolean() || condition.getAsBoolean();
    }
}
