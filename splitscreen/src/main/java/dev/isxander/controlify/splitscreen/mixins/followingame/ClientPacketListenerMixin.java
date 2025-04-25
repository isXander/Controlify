package dev.isxander.controlify.splitscreen.mixins.followingame;

import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void forceSplitscreenToJoin(CallbackInfo ci) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            String host;
            int port;

            if (this.minecraft.hasSingleplayerServer()) {
                IntegratedServer server = this.minecraft.getSingleplayerServer();

                if (!server.isPublished()) {
                    port = HttpUtil.getAvailablePort();
                    server.publishServer(GameType.DEFAULT_MODE, false, port);
                } else {
                    port = server.getPort();
                }

                host = InetAddress.getLoopbackAddress().getHostAddress();
            } else {
                var address = ServerAddress.parseString(this.minecraft.getCurrentServer().ip);
                host = address.getHost();
                port = address.getPort();
            }

            String finalHost = host;
            controller.forEachPawn(pawn -> pawn.joinServer(finalHost, port));
        });
    }
}
