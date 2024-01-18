package dev.isxander.controlify.mixins.feature.rumble.damage;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends PlayerMixin {
    // the goal is only one of these runs.
    // because they're both on the same tick, try and rely on the hurttime being less to
    // indicate which is the first/only to run

    // both can run, or either will run, but we want this to trigger once.
    // if both run, hurt time will be set to 10 on the first, so we know if it is >=10 it has already ran

    // runs first, but only if the health actually runs down
    @Override
    protected void onHealthChangedDamage(DamageSource source, CallbackInfo ci) {
        if (hurtTime < 10) {
            doDamageRumble();
        }
    }

    // runs second, but only if the damage was caused by an entity
    @Override
    protected void onEntityHurtMeDamage(float yaw, CallbackInfo ci) {
        if (hurtTime < 10) {
            doDamageRumble();
        }
    }


    @Unique
    private void doDamageRumble() {
        ControlifyApi.get().getCurrentController().ifPresent(controller -> controller.rumbleManager().play(
                RumbleSource.DAMAGE,
                BasicRumbleEffect.constant(0.8f, 0.5f, 5)
        ));
    }
}
