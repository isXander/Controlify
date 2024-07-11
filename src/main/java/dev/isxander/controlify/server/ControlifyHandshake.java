package dev.isxander.controlify.server;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class ControlifyHandshake {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final int PROTOCOL_VERSION = 1;
    public static final ResourceLocation HANDSHAKE_CHANNEL = CUtil.rl("handshake");

    private static final ControlifyPacketCodec<HandshakePacket> handshakePacketCodec = ControlifyPacketCodec.of(
            (buf, packet) -> buf.writeInt(packet.protocolVersion()),
            buf -> new HandshakePacket(buf.readInt())
    );

    public static void setupOnServer() {
        PlatformMainUtil.setupServersideHandshake(
                HANDSHAKE_CHANNEL,
                handshakePacketCodec,
                handshakePacketCodec,
                () -> new HandshakePacket(PROTOCOL_VERSION),
                (packet, handler) -> {
                    if (packet == null) {
                        // client does not have controlify installed
                        return;
                    }

                    if (packet.protocolVersion() > PROTOCOL_VERSION) {
                        handler.disconnect(Component.literal("Server has an old version of Controlify installed and is incompatible with this client.").withStyle(ChatFormatting.RED));
                    } else if (packet.protocolVersion() < PROTOCOL_VERSION) {
                        handler.disconnect(Component.literal("Client has an old version of Controlify installed and is incompatible with this server.").withStyle(ChatFormatting.RED));
                    }
                }
        );
    }

    public static void setupOnClient() {
        PlatformClientUtil.setupClientsideHandshake(
                HANDSHAKE_CHANNEL,
                handshakePacketCodec,
                handshakePacketCodec,
                inboundHandshake -> new HandshakePacket(PROTOCOL_VERSION)
        );
    }

    private record HandshakePacket(int protocolVersion) {}
}
