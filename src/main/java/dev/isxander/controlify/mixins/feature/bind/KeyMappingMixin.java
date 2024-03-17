package dev.isxander.controlify.mixins.feature.bind;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.isxander.controlify.bindings.KeyMappingOverride;
import dev.isxander.controlify.bindings.KeyMappingOverrideHolder;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(KeyMapping.class)
public class KeyMappingMixin implements KeyMappingOverrideHolder {
    @Unique private final List<KeyMappingOverride> overrides = new ArrayList<>();

    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean injectOverrideState(boolean keyMappingState) {
        return keyMappingState || overrides.stream().anyMatch(KeyMappingOverride::isDown);
    }

    @Override
    public void controlify$addOverride(KeyMappingOverride override) {
        this.overrides.add(override);
    }
}
