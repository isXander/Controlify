package dev.isxander.controlify.mixins.compat.screen.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSelectionList.class)
public class AbstractSelectionListMixin {
    @ModifyExpressionValue(method = "setFocused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/InputType;isKeyboard()Z"))
    private boolean shouldEnsureEntryVisible(boolean keyboard) {
        return keyboard || Controlify.instance().currentInputMode() == InputMode.CONTROLLER;
    }
}
