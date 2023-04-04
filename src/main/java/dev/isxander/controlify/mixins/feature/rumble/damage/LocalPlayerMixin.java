package dev.isxander.controlify.mixins.feature.rumble.damage;

import com.mojang.authlib.GameProfile;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    @Unique private float lastHealth = getHealth();
    @Unique private boolean skipTick = true;

    public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkHealthTick(CallbackInfo ci) {
        var damageTaken = Math.max(0, lastHealth - getHealth());
        lastHealth = getHealth();

        if (damageTaken > 0 && !skipTick) {
            float minMagnitude = 0.4f;
            float smallestDamage = 2; // the damage that results in minMagnitude
            float maxDamage = 15; // the damage that results in magnitude 1.0f

            float magnitude = (Mth.clamp(damageTaken, smallestDamage, maxDamage) - smallestDamage) / (maxDamage - smallestDamage) * (1 - minMagnitude) + minMagnitude;
            System.out.println(magnitude);
            ControlifyApi.get().currentController().rumbleManager().play(
                    BasicRumbleEffect.constant(magnitude, 0f, magnitude >= 0.75f ? 8 : 5)
            );
        }
        // skip first tick from spawn
        skipTick = false;
    }
}
