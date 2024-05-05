package dev.isxander.controlify.splitscreen.protocol.packets.play;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public interface BidirectionalPlayPacketListener extends PacketListener {
    static <T extends BidirectionalPlayPacketListener, B extends FriendlyByteBuf> ProtocolInfoBuilder<T, B> withBidirectionalPackets(ProtocolInfoBuilder<T, B> builder) {
        return builder;
    }
}
