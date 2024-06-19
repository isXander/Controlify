package dev.isxander.controlify.splitscreen.protocol.packets.handshake;

import dev.isxander.controlify.utils.CUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public record ControllerboundHandshakePacket(int protocolVersion) implements Packet<ControllerHandshakePacketListener> {
    public static final StreamCodec<ByteBuf, ControllerboundHandshakePacket> CODEC = ByteBufCodecs.INT
            .map(ControllerboundHandshakePacket::new, ControllerboundHandshakePacket::protocolVersion);
    public static final PacketType<ControllerboundHandshakePacket> TYPE =
            new PacketType<>(PacketFlow.SERVERBOUND, CUtil.rl("splitscreen_handshake"));

    @Override
    public PacketType<? extends Packet<ControllerHandshakePacketListener>> type() {
        return TYPE;
    }

    @Override
    public void handle(ControllerHandshakePacketListener handler) {
        handler.handleHandshake(this);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
