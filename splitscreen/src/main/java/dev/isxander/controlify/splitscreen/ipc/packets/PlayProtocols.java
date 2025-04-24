package dev.isxander.controlify.splitscreen.ipc.packets;

import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play.*;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.*;
import dev.isxander.controlify.splitscreen.host.ipc.ControllerPlayPacketListener;
import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
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
                            .addPacket(ControllerboundGiveMeFocusIfForegroundPacket.TYPE, ControllerboundGiveMeFocusIfForegroundPacket.CODEC)
            ).bind(FriendlyByteBuf::new);

    public static final ProtocolInfo<PawnPlayPacketListener> PAWNBOUND =
            ProtocolInfoBuilder.<PawnPlayPacketListener, FriendlyByteBuf>clientboundProtocol(
                    ConnectionProtocol.PLAY,
                    builder -> CommonProtocols.addPawnboundPackets(builder)
                            .addPacket(PawnboundKeepAlivePacket.TYPE, PawnboundKeepAlivePacket.CODEC)
                            .addPacket(PawnboundJoinServerPacket.TYPE, PawnboundJoinServerPacket.CODEC)
                            .addPacket(PawnboundParentWindowPacket.TYPE, PawnboundParentWindowPacket.CODEC)
                            .addPacket(PawnboundSplitscreenPositionPacket.TYPE, PawnboundSplitscreenPositionPacket.CODEC)
                            .addPacket(PawnboundWindowFocusStatePacket.TYPE, PawnboundWindowFocusStatePacket.CODEC)
                            .addPacket(PawnboundCloseGamePacket.TYPE, PawnboundCloseGamePacket.CODEC)
            ).bind(FriendlyByteBuf::new);
}
