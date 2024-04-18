package dev.isxander.controlify.mixins.feature.font;

import dev.isxander.controlify.font.BindComponentContents;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ComponentSerialization.class)
public class ComponentSerializationMixin {
    @ModifyVariable(method = "createCodec", at = @At(value = "STORE", ordinal = 0))
    private static ComponentContents.Type<?>[] addCustomContentType(ComponentContents.Type<?>[] types) {
        return ArrayUtils.add(types, BindComponentContents.TYPE);
    }
}
