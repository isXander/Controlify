package dev.isxander.controlify.splitscreen.protocol.packets.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public class CommonProtocols {
    public static <T extends ControllerboundCommonPacketListener, B extends FriendlyByteBuf> ProtocolInfoBuilder<T, B> addControllerboundPackets(ProtocolInfoBuilder<T, B> builder) {
        return builder;
    }

    public static <T extends PawnboundCommonPacketListener, B extends FriendlyByteBuf> ProtocolInfoBuilder<T, B> addPawnboundPackets(ProtocolInfoBuilder<T, B> builder) {
        return builder
                .addPacket(PawnboundDisconnectPacket.TYPE, PawnboundDisconnectPacket.CODEC);
    }
}
