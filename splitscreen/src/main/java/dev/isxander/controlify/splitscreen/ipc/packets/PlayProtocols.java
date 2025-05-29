package dev.isxander.controlify.splitscreen.ipc.packets;

import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play.*;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.*;
import dev.isxander.controlify.splitscreen.host.ipc.ControllerPlayPacketListener;
import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public final class PlayProtocols {

    public static ProtocolInfo<ControllerPlayPacketListener> controllerbound(StreamCodec<FriendlyByteBuf, ControllerboundEngineCustomPayloadPacket> engineCodec) {
        return ProtocolInfoBuilder.<ControllerPlayPacketListener, FriendlyByteBuf>serverboundProtocol(
                ConnectionProtocol.PLAY,
                builder -> CommonProtocols.addControllerboundPackets(builder)
                        .addPacket(ControllerboundHelloPacket.TYPE, ControllerboundHelloPacket.CODEC)
                        .addPacket(ControllerboundKeepAlivePacket.TYPE, ControllerboundKeepAlivePacket.CODEC)
                        .addPacket(ControllerboundGiveMeFocusIfForegroundPacket.TYPE, ControllerboundGiveMeFocusIfForegroundPacket.CODEC)
                        .addPacket(ControllerboundSignalReadyPacket.TYPE, ControllerboundSignalReadyPacket.CODEC)
                        .addPacket(ControllerboundServerDisconnectedPacket.TYPE, ControllerboundServerDisconnectedPacket.CODEC)
                        .addPacket(ControllerboundEngineCustomPayloadPacket.TYPE, engineCodec)
        ).bind(FriendlyByteBuf::new);
    }

    public static ProtocolInfo<PawnPlayPacketListener> pawnbound(StreamCodec<FriendlyByteBuf, PawnboundEngineCustomPayloadPacket> engineCodec) {
        return ProtocolInfoBuilder.<PawnPlayPacketListener, FriendlyByteBuf>clientboundProtocol(
                ConnectionProtocol.PLAY,
                builder -> CommonProtocols.addPawnboundPackets(builder)
                        .addPacket(PawnboundKeepAlivePacket.TYPE, PawnboundKeepAlivePacket.CODEC)
                        .addPacket(PawnboundJoinServerPacket.TYPE, PawnboundJoinServerPacket.CODEC)
                        .addPacket(PawnboundCloseGamePacket.TYPE, PawnboundCloseGamePacket.CODEC)
                        .addPacket(PawnboundUseControllerPacket.TYPE, PawnboundUseControllerPacket.CODEC)
                        .addPacket(PawnboundServerDisconnectPacket.TYPE, PawnboundServerDisconnectPacket.CODEC)
                        .addPacket(PawnboundLoadConfigPacket.TYPE, PawnboundLoadConfigPacket.CODEC)
                        .addPacket(PawnboundEngineCustomPayloadPacket.TYPE, engineCodec)
        ).bind(FriendlyByteBuf::new);
    }
}
