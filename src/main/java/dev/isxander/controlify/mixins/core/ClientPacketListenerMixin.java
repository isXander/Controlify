package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
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
public class ClientPacketListenerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleLogin", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/Input;", opcode = Opcodes.ASTORE, shift = At.Shift.AFTER))
    private void useControllerInput(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (Controlify.getInstance().getCurrentInputMode() == InputMode.CONTROLLER && minecraft.player != null)
            minecraft.player.input = new ControllerPlayerMovement(Controlify.getInstance().getCurrentController());
    }
}
