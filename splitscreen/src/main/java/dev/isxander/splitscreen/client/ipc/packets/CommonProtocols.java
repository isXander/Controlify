package dev.isxander.splitscreen.client.ipc.packets;

import dev.isxander.splitscreen.client.remote.ipc.PawnboundCommonPacketListener;
import dev.isxander.splitscreen.client.ipc.packets.pawnbound.common.PawnboundDisconnectPacket;
import dev.isxander.splitscreen.client.host.ipc.ControllerboundCommonPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.util.Unit;

public final class CommonProtocols {

    public static <T extends ControllerboundCommonPacketListener, B extends FriendlyByteBuf> ProtocolInfoBuilder<T, B, Unit> addControllerboundPackets(ProtocolInfoBuilder<T, B, Unit> builder) {
        return builder;
    }

    public static <T extends PawnboundCommonPacketListener, B extends FriendlyByteBuf> ProtocolInfoBuilder<T, B, Unit> addPawnboundPackets(ProtocolInfoBuilder<T, B, Unit> builder) {
        return builder
                .addPacket(PawnboundDisconnectPacket.TYPE, PawnboundDisconnectPacket.CODEC);
    }
}
