package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import dev.isxander.controlify.ingame.DualInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Override input handling for main player.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(client, connection, commonListenerCookie);
    }

    @Inject(method = "handleLogin", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/Input;", opcode = Opcodes.ASTORE, shift = At.Shift.AFTER))
    private void overrideNewPlayerInput(ClientboundLoginPacket packet, CallbackInfo ci) {
        ControllerPlayerMovement.updatePlayerInput(minecraft.player);
    }

    @Inject(method = "handleRespawn", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/Input;", opcode = Opcodes.ASTORE, shift = At.Shift.AFTER))
    private void overrideRespawnInput(ClientboundRespawnPacket packet, CallbackInfo ci, @Local(ordinal = 1) LocalPlayer newPlayer) {
        ControllerPlayerMovement.updatePlayerInput(newPlayer);
    }
}
