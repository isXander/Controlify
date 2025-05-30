package dev.isxander.splitscreen.ipc.packets.controllerbound.play;

import dev.isxander.splitscreen.host.ipc.ControllerPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundServerDisconnectedPacket(Component disconnectReason) implements ControllerboundPlayPacket {
    public static final StreamCodec<FriendlyByteBuf, ControllerboundServerDisconnectedPacket> CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
            ControllerboundServerDisconnectedPacket::disconnectReason,
            ControllerboundServerDisconnectedPacket::new
    );
    public static final PacketType<ControllerboundServerDisconnectedPacket> TYPE =
            ControllerboundPlayPacket.createType("server_disconnected");

    @Override
    public void handle(ControllerPlayPacketListener handler) {
        handler.handleServerDisconnected(this);
    }

    @Override
    public PacketType<ControllerboundServerDisconnectedPacket> type() {
        return TYPE;
    }
}
