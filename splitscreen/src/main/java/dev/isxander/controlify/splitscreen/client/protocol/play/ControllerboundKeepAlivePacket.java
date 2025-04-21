package dev.isxander.controlify.splitscreen.client.protocol.play;

import dev.isxander.controlify.splitscreen.server.protocol.play.ControllerPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundKeepAlivePacket() implements ControllerboundPlayPacket {
    public static final ControllerboundKeepAlivePacket INSTANCE = new ControllerboundKeepAlivePacket();
    public static final StreamCodec<FriendlyByteBuf, ControllerboundKeepAlivePacket> CODEC = StreamCodec.unit(INSTANCE);
    public static final PacketType<ControllerboundKeepAlivePacket> TYPE = ControllerboundPlayPacket.createType("keep_alive");

    @Override
    public void handle(ControllerPlayPacketListener handler) {
        handler.handleKeepAlive(this);
    }

    @Override
    public PacketType<? extends Packet<ControllerPlayPacketListener>> type() {
        return TYPE;
    }
}
