package dev.isxander.controlify.mixins.feature.bind;

import net.minecraft.client.KeyMapping;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Accessor("ALL")
    static Map<String, KeyMapping> getAll() {
        throw new NotImplementedException("Should be overwritten by Accessor");
    }
}
