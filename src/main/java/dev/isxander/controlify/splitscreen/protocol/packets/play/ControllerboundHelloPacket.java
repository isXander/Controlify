package dev.isxander.controlify.splitscreen.protocol.packets.play;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundHelloPacket() implements Packet<ControllerPlayPacketListener> {
    public static final ControllerboundHelloPacket INSTANCE = new ControllerboundHelloPacket();
    public static final StreamCodec<FriendlyByteBuf, ControllerboundHelloPacket> CODEC = StreamCodec.unit(INSTANCE);
    public static final PacketType<ControllerboundHelloPacket> TYPE = ControllerboundPlayPacket.createType("hello");

    @Override
    public void handle(ControllerPlayPacketListener handler) {
        handler.handleHello(this);
    }

    @Override
    public PacketType<? extends Packet<ControllerPlayPacketListener>> type() {
        return TYPE;
    }
}
