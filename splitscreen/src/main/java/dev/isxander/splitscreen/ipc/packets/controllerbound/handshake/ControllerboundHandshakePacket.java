package dev.isxander.splitscreen.ipc.packets.controllerbound.handshake;

import dev.isxander.splitscreen.host.ipc.ControllerHandshakePacketListener;
import dev.isxander.splitscreen.util.CSUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundHandshakePacket(int protocolVersion) implements Packet<ControllerHandshakePacketListener> {
    public static final StreamCodec<ByteBuf, ControllerboundHandshakePacket> CODEC =
            ByteBufCodecs.INT.map(ControllerboundHandshakePacket::new, ControllerboundHandshakePacket::protocolVersion);
    public static final PacketType<ControllerboundHandshakePacket> TYPE = new PacketType<>(PacketFlow.SERVERBOUND, CSUtil.rl("handshake"));

    @Override
    public void handle(ControllerHandshakePacketListener handler) {
        handler.handleHandshake(this);
    }

    @Override
    public PacketType<? extends Packet<ControllerHandshakePacketListener>> type() {
        return TYPE;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
