package dev.isxander.controlify.mixins.feature.patches.keymessages;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DemoIntroScreen.class)
public class DemoIntroScreenMixin {
    @Definition(id = "keyUp", field = "Lnet/minecraft/client/Options;keyUp:Lnet/minecraft/client/KeyMapping;")
    @Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
    @Expression("?.keyUp.getTranslatedKeyMessage()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Component useUpGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.WALK_FORWARD.bindId(), original);
    }

    @Definition(id = "keyLeft", field = "Lnet/minecraft/client/Options;keyLeft:Lnet/minecraft/client/KeyMapping;")
    @Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
    @Expression("?.keyLeft.getTranslatedKeyMessage()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Component useLeftGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.WALK_LEFT.bindId(), original);
    }

    @Definition(id = "keyDown", field = "Lnet/minecraft/client/Options;keyDown:Lnet/minecraft/client/KeyMapping;")
    @Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
    @Expression("?.keyDown.getTranslatedKeyMessage()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Component useDownGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.WALK_BACKWARD.bindId(), original);
    }

    @Definition(id = "keyRight", field = "Lnet/minecraft/client/Options;keyRight:Lnet/minecraft/client/KeyMapping;")
    @Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
    @Expression("?.keyRight.getTranslatedKeyMessage()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Component useRightGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.WALK_RIGHT.bindId(), original);
    }

    @Definition(id = "keyJump", field = "Lnet/minecraft/client/Options;keyJump:Lnet/minecraft/client/KeyMapping;")
    @Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
    @Expression("?.keyJump.getTranslatedKeyMessage()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Component useJumpGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.JUMP.bindId(), original);
    }

    @Definition(id = "keyInventory", field = "Lnet/minecraft/client/Options;keyInventory:Lnet/minecraft/client/KeyMapping;")
    @Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
    @Expression("?.keyInventory.getTranslatedKeyMessage()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Component useInventoryGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.INVENTORY.bindId(), original);
    }
}
