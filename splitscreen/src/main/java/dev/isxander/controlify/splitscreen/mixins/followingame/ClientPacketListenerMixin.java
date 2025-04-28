package dev.isxander.controlify.splitscreen.mixins.followingame;

import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.host.util.LANUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void forceSplitscreenToJoin(CallbackInfo ci) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            ServerAddress address;

            if (this.minecraft.hasSingleplayerServer()) {
                IntegratedServer server = this.minecraft.getSingleplayerServer();
                address = LANUtil.getOrPublishLANServer(server);
            } else {
                address = ServerAddress.parseString(this.minecraft.getCurrentServer().ip);
            }

            controller.forEachPawn(pawn -> pawn.joinServer(address.getHost(), address.getPort()));
        });
    }
}
