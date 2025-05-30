package dev.isxander.splitscreen.server.login;

import com.mojang.logging.LogUtils;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.config.SplitscreenConfig;
import dev.isxander.splitscreen.client.host.SplitscreenController;
import dev.isxander.splitscreen.client.host.gui.SplitscreenDisconnectedScreen;
import dev.isxander.splitscreen.server.mixins.login.ClientHandshakePacketListenerImplAccessor;
import dev.isxander.splitscreen.server.mixins.login.DisconnectedScreenAccessor;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchArguments;
import dev.isxander.splitscreen.client.remote.RemotePawnMain;
import dev.isxander.splitscreen.server.login.packets.ClientboundIdentifyPacket;
import dev.isxander.splitscreen.server.login.packets.ClientboundNoncePacket;
import dev.isxander.splitscreen.server.login.packets.ServerboundIdentifyPacket;
import dev.isxander.splitscreen.server.login.packets.ServerboundNonceAckPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SplitscreenLoginFlowClient {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        // Only register the login flow if splitscreen has been enabled
        SplitscreenBootstrapper.getSide().ifPresent(side -> {
            Optional<SplitscreenController> controllerOpt = SplitscreenBootstrapper.getController();
            Optional<RemotePawnMain> pawnOpt = SplitscreenBootstrapper.getPawn();

            ClientLoginNetworking.registerGlobalReceiver(SplitscreenLoginFlowServer.CHANNEL_IDENTIFY, (client, listener, buf, sender) -> {
                LOGGER.info("Received splitscreen identify packet. This server supports Splitscreen!");

                ClientboundIdentifyPacket packet = ClientboundIdentifyPacket.STREAM_CODEC.decode(buf);
                int requestedProtocolVersion = packet.protocolVersion();
                int supportedProtocolVersion = SplitscreenLoginFlowServer.PROTOCOL_VERSION;
                if (requestedProtocolVersion != supportedProtocolVersion) {
                    LOGGER.error("Requested splitscreen protocol version {} does not match supported version {}", requestedProtocolVersion, supportedProtocolVersion);
                    return CompletableFuture.completedFuture(null);
                }

                ClientIdentification identification = switch (side) {
                    case CONTROLLER -> {
                        SplitscreenController controller = controllerOpt.orElseThrow();

                        int subPlayerCount = controller.getPawnCount(false);
                        LOGGER.info("Identifying as controller with {} sub-players", subPlayerCount);

                        yield new ClientIdentification.Controller(subPlayerCount, SplitscreenConfig.INSTANCE.createSharedConfig());
                    }
                    case PAWN -> {
                        RemotePawnMain pawn = pawnOpt.orElseThrow();

                        UUID controllerUuid = RelaunchArguments.HOST_UUID.get().orElseThrow();
                        int subPlayerIndex = RelaunchArguments.PAWN_INDEX.get().orElseThrow() - 1;
                        byte[] nonce = pawn.getPawn().getLastLoginNonce();
                        byte[] hmac = SplitscreenLoginFlowServer.generateHmac(nonce, controllerUuid, subPlayerIndex);
//                        LOGGER.info("Client HMAC: controller UUID {}, sub-player index {}, nonce {}", controllerUuid, subPlayerIndex, Hex.encodeHexString(nonce));

                        LOGGER.info("Identifying as pawn with controller UUID {} and sub-player index {}", controllerUuid, subPlayerIndex);

                        yield new ClientIdentification.Pawn(controllerUuid, hmac, subPlayerIndex);
                    }
                };

                return CompletableFuture.completedFuture(new ServerboundIdentifyPacket(identification).encode());
            });

            controllerOpt.ifPresent(controller -> {
                ClientLoginNetworking.registerGlobalReceiver(SplitscreenLoginFlowServer.CHANNEL_CONTROLLER, (client, listener, buf, sender) -> {
                    LOGGER.info("Received nonce packet.");

                    ClientboundNoncePacket packet = ClientboundNoncePacket.STREAM_CODEC.decode(buf);
                    byte[] nonce = packet.nonce();

                    controllerOpt.get().getLocalPawn().setLastLoginNonce(nonce);

                    if (!client.hasSingleplayerServer()) {
                        ServerAddress address = ServerAddress.parseString(((ClientHandshakePacketListenerImplAccessor) listener).getServerData().ip);
                        controller.forEachPawn(pawn -> pawn.joinServer(address.getHost(), address.getPort(), controller.getLocalPawn().getLastLoginNonce()));
                    }

                    return CompletableFuture.completedFuture(new ServerboundNonceAckPacket().encode());
                });
            });

            ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen instanceof DisconnectedScreenAccessor accessor && !(screen instanceof SplitscreenDisconnectedScreen)) {
                    SplitscreenBootstrapper.getControllerBridge().orElseThrow().serverDisconnected(accessor.getDetails().reason());
                }
            });
        });
    }
}
