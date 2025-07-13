package dev.isxander.splitscreen.client.ipc.packets.controllerbound.play;

import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.host.ipc.ControllerPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundHelloPacket(InputMethod inputMethod) implements ControllerboundPlayPacket {
    public static final StreamCodec<FriendlyByteBuf, ControllerboundHelloPacket> CODEC =
            StreamCodec.composite(
                    InputMethod.STREAM_CODEC,
                    ControllerboundHelloPacket::inputMethod,
                    ControllerboundHelloPacket::new
            );
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
