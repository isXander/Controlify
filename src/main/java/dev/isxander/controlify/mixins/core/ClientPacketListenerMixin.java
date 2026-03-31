package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Override input handling for main player.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Definition(id = "input", field = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/ClientInput;")
    @Definition(id = "minecraft", field = "Lnet/minecraft/client/multiplayer/ClientPacketListener;minecraft:Lnet/minecraft/client/Minecraft;")
    @Definition(id = "player", field = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;")
    @Expression("?.minecraft.player.input = ?")
    @Inject(method = "handleLogin", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void overrideNewPlayerInput(ClientboundLoginPacket packet, CallbackInfo ci) {
        ControllerPlayerMovement.updatePlayerInput(Minecraft.getInstance().player);
    }

    @Definition(id = "newPlayer", local = @Local(type = LocalPlayer.class, name = "newPlayer"))
    @Definition(id = "input", field = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/ClientInput;")
    @Expression("newPlayer.input = ?")
    @Inject(method = "handleRespawn", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void overrideRespawnInput(ClientboundRespawnPacket packet, CallbackInfo ci, @Local(name = "newPlayer") LocalPlayer newPlayer) {
        ControllerPlayerMovement.updatePlayerInput(newPlayer);
    }
}
