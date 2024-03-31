package dev.isxander.controlify.mixins.feature.rumble.useitem;

import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.effects.UseItemEffectHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleRespawn", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", opcode = Opcodes.PUTFIELD, shift = At.Shift.BEFORE))
    private void clearUseItemRumble(ClientboundRespawnPacket packet, CallbackInfo ci) {
        ContinuousRumbleEffect effect = ((UseItemEffectHolder) Minecraft.getInstance().player).controlify$getUseItemEffect();
        if (effect != null) effect.stop();
    }

}
