package dev.isxander.controlify.mixins.feature.guide.ingame;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Definition(id = "minecraft", field = "Lnet/minecraft/client/multiplayer/ClientPacketListener;minecraft:Lnet/minecraft/client/Minecraft;")
    @Definition(id = "player", field = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;")
    @Definition(id = "input", field = "Lnet/minecraft/client/player/LocalPlayer;input:Lnet/minecraft/client/player/ClientInput;")
    @Expression("?.minecraft.player.input = ?")
    @Inject(method = "handleLogin", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void buttonGuideLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        initButtonGuide();
    }

    @Inject(method = "handleRespawn", at = @At("RETURN"))
    private void buttonGuideRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        initButtonGuide();
    }

    @Unique
    private void initButtonGuide() {
        var minecraft = Minecraft.getInstance();
        if (Controlify.instance().currentInputMode().isController() && minecraft.player != null)
            Controlify.instance().inGameButtonGuide = new InGameButtonGuide(Controlify.instance().getCurrentController().orElseThrow(), minecraft);
    }
}
