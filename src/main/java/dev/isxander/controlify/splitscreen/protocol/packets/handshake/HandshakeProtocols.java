package dev.isxander.controlify.splitscreen.protocol.packets.handshake;

import dev.isxander.controlify.splitscreen.protocol.packets.common.CommonProtocols;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public final class HandshakeProtocols {
    public static final ProtocolInfo<ControllerHandshakePacketListener> CONTROLLERBOUND = ProtocolInfoBuilder.serverboundProtocol(
            ConnectionProtocol.HANDSHAKING,
            builder -> CommonProtocols.addControllerboundPackets(builder)
                    .addPacket(ControllerboundHandshakePacket.TYPE, ControllerboundHandshakePacket.CODEC)
    );


}
