package dev.isxander.controlify.platform.neoforge.mixins;

import dev.isxander.controlify.platform.neoforge.VanillaKeyMappingHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Options.class)
public class OptionsMixin implements VanillaKeyMappingHolder {
    @Shadow public KeyMapping[] keyMappings;

    @Unique private KeyMapping[] controlify$vanillaKeyMappings;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void storeVanillaKeybindsBeforeModification(CallbackInfo ci) {
        this.controlify$vanillaKeyMappings = Arrays.copyOf(keyMappings, keyMappings.length);
    }

    @Override
    public KeyMapping[] controlify$getVanillaKeys() {
        return controlify$vanillaKeyMappings;
    }
}
