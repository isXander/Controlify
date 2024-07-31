package dev.isxander.controlify.mixins.feature.font;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {
    @ModifyExpressionValue(
            method = "decompose",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/network/chat/contents/TranslatableContents;key:Ljava/lang/String;"
            )
    )
    private String replaceControllerActiveKey(String originalKey) {
        if (BindingFontHelper.PLACEHOLDER_CONTROLLER_ACTIVE_KEY.equals(originalKey)) {
            if (ControlifyApi.get().currentInputMode().isController()) {
                return BindingFontHelper.PLACEHOLDER_KEY;
            } else {
                // because the key doesn't exist, it will rely on the fallback text provided by this translatable contents
                return originalKey;
            }
        }

        return originalKey;
    }
}
