package dev.isxander.controlify.splitscreen.server;

import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.host.SplitscreenController;
import dev.isxander.controlify.splitscreen.server.login.SplitscreenLoginFlowClient;
import dev.isxander.controlify.splitscreen.server.play.sound.ClientboundBundledSoundEntityPacket;
import dev.isxander.controlify.splitscreen.server.play.sound.ClientboundBundledSoundPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;

public class SplitscreenSSClient {
    public static void init() {
        SplitscreenSSServer.init();
        SplitscreenLoginFlowClient.init();
        initSound();
    }

    private static void initSound() {
        // only controllers will receive these
        ClientPlayNetworking.registerGlobalReceiver(ClientboundBundledSoundPacket.TYPE, (payload, ctx) -> {
            SplitscreenController controller = SplitscreenBootstrapper.getController().orElseThrow(() -> new IllegalStateException("Server sent bundled sound packet to non-controller."));

            BundledPacketInfo bundleInfo = payload.bundleInfo();
            ClientboundSoundPacket soundPacket = payload.packet();

            // all this does is play all sounds from the perspective of the first player.
            if (bundleInfo.includeController()) {
                soundPacket.handle(ctx.client().getConnection());
            } // TODO: we can do better than this
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundBundledSoundEntityPacket.TYPE, (payload, ctx) -> {
            SplitscreenController controller = SplitscreenBootstrapper.getController().orElseThrow(() -> new IllegalStateException("Server sent bundled sound packet to non-controller."));

            BundledPacketInfo bundleInfo = payload.bundleInfo();
            ClientboundSoundEntityPacket soundPacket = payload.packet();

            // all this does is play all sounds from the perspective of the first player.
            if (bundleInfo.includeController()) {
                soundPacket.handle(ctx.client().getConnection());
            } // TODO: we can do better than this
        });
    }
}
