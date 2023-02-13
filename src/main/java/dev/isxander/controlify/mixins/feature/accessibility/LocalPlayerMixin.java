package dev.isxander.controlify.mixins.feature.accessibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object shouldUseAutoJump(Object keyboardAutoJump) {
        if (Controlify.instance().currentInputMode() == InputMode.CONTROLLER) {
            return Controlify.instance().currentController().config().autoJump;
        }
        return keyboardAutoJump;
    }
}
