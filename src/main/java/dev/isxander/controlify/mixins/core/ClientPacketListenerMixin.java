package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Override input handling for main player.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/Input;", opcode = Opcodes.ASTORE, shift = At.Shift.AFTER))
    private void overrideNewPlayerInput(ClientboundLoginPacket packet, CallbackInfo ci) {
        ControllerPlayerMovement.updatePlayerInput(Minecraft.getInstance().player);
    }

    @Inject(method = "handleRespawn", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/Input;", opcode = Opcodes.ASTORE, shift = At.Shift.AFTER))
    private void overrideRespawnInput(ClientboundRespawnPacket packet, CallbackInfo ci, @Local(ordinal = 1) LocalPlayer newPlayer) {
        ControllerPlayerMovement.updatePlayerInput(newPlayer);
    }
}
