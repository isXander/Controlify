package dev.isxander.splitscreen.ipc.packets.controllerbound.play;

import dev.isxander.splitscreen.host.ipc.ControllerPlayPacketListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundSignalReadyPacket(boolean finished, float progress) implements ControllerboundPlayPacket {
    public static final StreamCodec<ByteBuf, ControllerboundSignalReadyPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    ControllerboundSignalReadyPacket::finished,
                    ByteBufCodecs.FLOAT,
                    ControllerboundSignalReadyPacket::progress,
                    ControllerboundSignalReadyPacket::new
            );
    public static final PacketType<ControllerboundSignalReadyPacket> TYPE =
            ControllerboundPlayPacket.createType("signal_ready");

    @Override
    public void handle(ControllerPlayPacketListener handler) {
        handler.handleReadySignal(this);
    }

    @Override
    public PacketType<ControllerboundSignalReadyPacket> type() {
        return TYPE;
    }
}
