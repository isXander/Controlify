package dev.isxander.controlify.mixins.feature.guide.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/Input;", opcode = Opcodes.ASTORE, shift = At.Shift.AFTER))
    private void buttonGuideLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        initButtonGuide();
    }

    @Inject(method = "handleRespawn", at = @At("RETURN"))
    private void buttonGuideRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        initButtonGuide();
    }

    @Unique
    private void initButtonGuide() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (Controlify.instance().currentInputMode().isController() && player != null)
            Controlify.instance().inGameButtonGuide = new InGameButtonGuide(Controlify.instance().getCurrentController().orElseThrow(), player);
    }
}
