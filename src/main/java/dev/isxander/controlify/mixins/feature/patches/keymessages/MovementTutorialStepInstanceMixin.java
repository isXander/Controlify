package dev.isxander.controlify.mixins.feature.patches.keymessages;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.tutorial.MovementTutorialStepInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MovementTutorialStepInstance.class)
public class MovementTutorialStepInstanceMixin {
    @Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
    @Expression("key('forward')")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Component useForwardGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.WALK_FORWARD.bindId(), original);
    }

    @Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
    @Expression("key('left')")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Component useLeftGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.WALK_LEFT.bindId(), original);
    }

    @Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
    @Expression("key('back')")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Component useBackGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.WALK_BACKWARD.bindId(), original);
    }

    @Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
    @Expression("key('right')")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Component useRightGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.WALK_RIGHT.bindId(), original);
    }

    @Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
    @Expression("key('jump')")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Component useJumpGlyph(Component original) {
        return BindingFontHelper.bindingWithActiveFallback(ControlifyBindings.JUMP.bindId(), original);
    }

    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Expression("translatable('tutorial.look.description')")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static MutableComponent useLookMessage(MutableComponent original) {
        return Component.translatable("controlify.tutorial.look.description");
    }
}
