package dev.isxander.controlify.splitscreen.protocol;

import dev.isxander.controlify.splitscreen.server.protocol.play.ControllerPlayPacketListener;
import dev.isxander.controlify.splitscreen.client.protocol.play.ControllerboundHelloPacket;
import dev.isxander.controlify.splitscreen.client.protocol.play.ControllerboundKeepAlivePacket;
import dev.isxander.controlify.splitscreen.client.protocol.play.PawnPlayPacketListener;
import dev.isxander.controlify.splitscreen.server.protocol.play.PawnboundJoinServerPacket;
import dev.isxander.controlify.splitscreen.server.protocol.play.PawnboundKeepAlivePacket;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public final class PlayProtocols {

    public static final ProtocolInfo<ControllerPlayPacketListener> CONTROLLERBOUND =
            ProtocolInfoBuilder.<ControllerPlayPacketListener, FriendlyByteBuf>serverboundProtocol(
                    ConnectionProtocol.PLAY,
                    builder -> CommonProtocols.addControllerboundPackets(builder)
                            .addPacket(ControllerboundHelloPacket.TYPE, ControllerboundHelloPacket.CODEC)
                            .addPacket(ControllerboundKeepAlivePacket.TYPE, ControllerboundKeepAlivePacket.CODEC)
            ).bind(FriendlyByteBuf::new);

    public static final ProtocolInfo<PawnPlayPacketListener> PAWNBOUND =
            ProtocolInfoBuilder.<PawnPlayPacketListener, FriendlyByteBuf>clientboundProtocol(
                    ConnectionProtocol.PLAY,
                    builder -> CommonProtocols.addPawnboundPackets(builder)
                            .addPacket(PawnboundKeepAlivePacket.TYPE, PawnboundKeepAlivePacket.CODEC)
                            .addPacket(PawnboundJoinServerPacket.TYPE, PawnboundJoinServerPacket.CODEC)
            ).bind(FriendlyByteBuf::new);
}
