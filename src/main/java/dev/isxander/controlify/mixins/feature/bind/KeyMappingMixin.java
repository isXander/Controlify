package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.bindings.KeyMappingHandle;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyMapping.class)
public class KeyMappingMixin implements KeyMappingHandle {

    @Shadow
    protected int clickCount;

    @Shadow
    private boolean isDown;

    @Override
    public void controlify$setPressed(boolean isDown) {
        if (isDown) {
            this.isDown = true;
            this.clickCount++;
        } else {
            this.isDown = false;
        }
    }
}
