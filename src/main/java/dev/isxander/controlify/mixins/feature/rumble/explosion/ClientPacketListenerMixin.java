package dev.isxander.controlify.mixins.feature.rumble.explosion;

import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleExplosion", at = @At("RETURN"))
    private void onClientExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        float initialMagnitude = calculateMagnitude(packet);

        ControlifyApi.get().currentController().rumbleManager().play(
                RumbleSource.EXPLOSION,
                BasicRumbleEffect.join(
                        BasicRumbleEffect.constant(initialMagnitude, initialMagnitude, 4), // initial boom
                        BasicRumbleEffect.byTime(t -> {
                            float magnitude = calculateMagnitude(packet);
                            return new RumbleState(0f, magnitude - t * magnitude);
                        }, 20) // explosion
                )
        );
    }

    private float calculateMagnitude(ClientboundExplodePacket packet) {
        float distanceSqr = Math.max(
                (float)minecraft.player.distanceToSqr(packet.getX(), packet.getY(), packet.getZ())
                        - packet.getPower() * packet.getPower(), // power is explosion radius
                0f);
        float maxDistanceSqr = 4096f; // client only receives explosion packets within 64 blocks

        return 1f - Easings.easeOutQuad(distanceSqr / maxDistanceSqr);
    }
}
