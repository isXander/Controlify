package dev.isxander.controlify.mixins.feature.screenop.impl.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSelectionList.class)
public class AbstractSelectionListMixin {
    // Ensures that when changing focus within a list, the focused entry is made visible.
    // Usually this is gated for keyboard input only, but we want it for controller input too.
    // In 1.21.9, this call was completely removed.
    //? if <1.21.9 {
    /*@ModifyExpressionValue(method = "setFocused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/InputType;isKeyboard()Z"))
    private boolean shouldEnsureEntryVisible(boolean keyboard) {
        return keyboard || Controlify.instance().currentInputMode().isController();
    }
    *///?}
}
