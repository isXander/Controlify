package dev.isxander.controlify.splitscreen.client.protocol.play;

import dev.isxander.controlify.splitscreen.server.protocol.play.ControllerPlayPacketListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundHelloPacket(long pawnWindowHandle) implements ControllerboundPlayPacket {
    public static final StreamCodec<ByteBuf, ControllerboundHelloPacket> CODEC =
            ByteBufCodecs.LONG.map(ControllerboundHelloPacket::new, ControllerboundHelloPacket::pawnWindowHandle);
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
