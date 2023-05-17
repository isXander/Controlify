package dev.isxander.controlify.mixins.feature.guide.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleLogin", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/Input;", opcode = Opcodes.ASTORE, shift = At.Shift.AFTER))
    private void buttonGuideLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        initButtonGuide();
    }

    @Inject(method = "handleRespawn", at = @At("RETURN"))
    private void buttonGuideRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        initButtonGuide();
    }

    private void initButtonGuide() {
        if (Controlify.instance().currentInputMode() == InputMode.CONTROLLER && minecraft.player != null)
            Controlify.instance().inGameButtonGuide = new InGameButtonGuide(Controlify.instance().getCurrentController().orElseThrow(), minecraft.player);
    }
}
