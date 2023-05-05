package dev.isxander.controlify.mixins.feature.oofinput;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import net.minecraft.client.renderer.GameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;pauseOnLostFocus:Z", opcode = Opcodes.GETFIELD))
    private boolean shouldPauseOnLossFocus(boolean original) {
        return original && !(Controlify.instance().config().globalSettings().outOfFocusInput && Controlify.instance().getCurrentController().isPresent());
    }
}
