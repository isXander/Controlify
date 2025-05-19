package dev.isxander.controlify.splitscreen.mixins.server.status;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.splitscreen.server.status.ServerStatusSplitscreenExt;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.multiplayer.ServerStatusPinger$1")
public class ServerStatusPingerListenerMixin {
    @Shadow @Final ServerData val$data;

    /**
     * Copy the splitscreen extension from the server status to the server data.
     */
    @ModifyExpressionValue(method = "handleStatusResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;status()Lnet/minecraft/network/protocol/status/ServerStatus;"))
    private ServerStatus addSplitscreenInfoToServerData(ServerStatus status) {
        ServerStatusSplitscreenExt.getExt(status).ifPresent(ext ->
                ServerStatusSplitscreenExt.setExt(val$data, ext));

        return status;
    }
}
