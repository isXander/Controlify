package dev.isxander.controlify.mixins.feature.rumble.damage;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.RumbleEffect;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "hurtTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setHealth(F)V", ordinal = 1))
    private void onClientHurt(float health, CallbackInfo ci) {
        // LivingEntity#hurt is server-side only, so we do it here
        doRumble();
    }

    @Inject(method = "hurtTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setHealth(F)V", ordinal = 0))
    private void onClientHealthUpdate(float health, CallbackInfo ci) {
        // for some reason fall damage calls hurtTo after the health has been updated at some point
        // this is called when hurtTo is set to the same health as the player already has
        doRumble();
    }

    private void doRumble() {
        ControlifyApi.get().currentController().rumbleManager().play(
                RumbleEffect.constant(0.5f, 0f, 5)
        );
    }
}
