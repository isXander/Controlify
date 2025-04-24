package dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play;

import dev.isxander.controlify.splitscreen.host.ipc.ControllerPlayPacketListener;
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
