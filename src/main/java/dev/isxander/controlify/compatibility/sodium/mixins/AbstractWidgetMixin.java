/*? if sodium {*/
package dev.isxander.controlify.compatibility.sodium.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import net.minecraft.client.InputType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import /*$ sodium-package >>*/ net.caffeinemc.mods.sodium .client.gui.widgets.AbstractWidget;

@Mixin(AbstractWidget.class)
public class AbstractWidgetMixin {
    @ModifyExpressionValue(method = "setFocused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getLastInputType()Lnet/minecraft/client/InputType;"))
    private InputType forceSetFocusedOnController(InputType type) {
        if (Controlify.instance().currentInputMode().isController()) {
            return InputType.KEYBOARD_ARROW;
        }
        return type;
    }
}
/*?}*/
