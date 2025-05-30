package dev.isxander.splitscreen.ipc.packets;

import dev.isxander.splitscreen.host.ipc.ControllerHandshakePacketListener;
import dev.isxander.splitscreen.ipc.packets.controllerbound.handshake.ControllerboundHandshakePacket;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public final class HandshakeProtocols {
    public static final ProtocolInfo<ControllerHandshakePacketListener> CONTROLLERBOUND =
            ProtocolInfoBuilder.<ControllerHandshakePacketListener, FriendlyByteBuf>serverboundProtocol(
                    ConnectionProtocol.HANDSHAKING,
                    builder -> CommonProtocols.addControllerboundPackets(builder)
                            .addPacket(ControllerboundHandshakePacket.TYPE, ControllerboundHandshakePacket.CODEC)
            ).bind(FriendlyByteBuf::new);
}
