package dev.isxander.controlify.mixins.feature.splitscreen;

import dev.isxander.controlify.Controlify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
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
    private void forceSplitscreenToJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        Controlify.instance().getSplitscreenMaster().ifPresent(master -> {
            if (minecraft.hasSingleplayerServer()) {
                IntegratedServer server = minecraft.getSingleplayerServer();

                int port;
                if (!server.isPublished()) {
                    port = HttpUtil.getAvailablePort();
                    server.publishServer(GameType.DEFAULT_MODE, false, port);
                } else {
                    port = server.getPort();
                }

                master.applyToPawns(slave -> slave.joinMyServer(port));
            }
        });

    }
}
