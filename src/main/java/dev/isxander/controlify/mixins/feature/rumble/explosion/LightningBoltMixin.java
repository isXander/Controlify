package dev.isxander.controlify.mixins.feature.rumble.explosion;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    private boolean onLightningStrike(boolean client) {
        if (client) {
            ControlifyApi.get().getCurrentController().ifPresent(controller -> controller.rumbleManager().play(
                    RumbleSource.EXPLOSION,
                    BasicRumbleEffect.join(
                            BasicRumbleEffect.constant(1f, 0.2f, 6), // initial boom
                            BasicRumbleEffect.byTime(t -> new RumbleState(0f, 1 - t*0.2f), 10) // explosion
                    )
            ));
        }
        return client;
    }
}
