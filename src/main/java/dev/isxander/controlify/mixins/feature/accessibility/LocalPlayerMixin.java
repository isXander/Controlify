package dev.isxander.controlify.mixins.feature.accessibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object shouldUseAutoJump(Object keyboardAutoJump) {
        if (ControlifyApi.get().currentInputMode().isController()) {
            return ControlifyApi.get().getCurrentController()
                    .map(controller -> controller.genericConfig().config().autoJump)
                    .orElse(false);
        }
        return keyboardAutoJump;
    }
}
