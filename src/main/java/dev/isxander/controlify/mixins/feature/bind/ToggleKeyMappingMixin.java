package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.bindings.KeyMappingHandle;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ToggleKeyMapping.class)
public abstract class ToggleKeyMappingMixin extends KeyMappingMixin implements KeyMappingHandle {

    @Shadow public abstract void setDown(boolean value);

    @Override
    public void controlify$setPressed(boolean isDown) {
        if (isDown)
            this.clickCount++;
        this.setDown(true);
    }
}
