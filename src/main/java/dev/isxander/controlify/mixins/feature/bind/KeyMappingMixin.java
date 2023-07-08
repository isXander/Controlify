package dev.isxander.controlify.mixins.feature.bind;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.bindings.KeyMappingOverrideHolder;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(KeyMapping.class)
public class KeyMappingMixin implements KeyMappingOverrideHolder {
    @Unique private final List<ControllerBinding> overrides = new ArrayList<>();

    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean injectOverrideState(boolean keyMappingState) {
        return keyMappingState || overrides.stream().anyMatch(override -> override.override() != null && override.override().toggleable().getAsBoolean() && override.held());
    }

    @Override
    public void controlify$addOverride(ControllerBinding binding) {
        this.overrides.add(binding);
    }
}
