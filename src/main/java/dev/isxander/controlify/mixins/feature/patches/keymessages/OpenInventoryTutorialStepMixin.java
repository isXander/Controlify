package dev.isxander.controlify.mixins.feature.patches.keymessages;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.tutorial.OpenInventoryTutorialStep;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(OpenInventoryTutorialStep.class)
public class OpenInventoryTutorialStepMixin {
    @Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
    @Expression("key('inventory')")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Component useGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.INVENTORY.bindId(), original);
    }
}
