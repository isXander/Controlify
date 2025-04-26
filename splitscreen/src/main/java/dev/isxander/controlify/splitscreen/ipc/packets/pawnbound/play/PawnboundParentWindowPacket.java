package dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
import dev.isxander.controlify.splitscreen.window.manager.NativeWindowHandle;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record PawnboundParentWindowPacket(
        NativeWindowHandle parentWindowHandle
) implements PawnboundPlayPacket {
    public static final StreamCodec<ByteBuf, PawnboundParentWindowPacket> CODEC =
            NativeWindowHandle.STREAM_CODEC.map(PawnboundParentWindowPacket::new, PawnboundParentWindowPacket::parentWindowHandle);
    public static final PacketType<PawnboundParentWindowPacket> TYPE =
            PawnboundPlayPacket.createType("parent_window");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleParentWindow(this);
    }

    @Override
    public PacketType<PawnboundParentWindowPacket> type() {
        return TYPE;
    }
}
