package dev.isxander.controlify.splitscreen.protocol.pawnbound.play;

import dev.isxander.controlify.splitscreen.client.protocol.PawnPlayPacketListener;
import dev.isxander.controlify.splitscreen.window.manager.NativeWindowHandle;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record PawnboundParentWindowPacket(
        NativeWindowHandle parentWindowHandle,
        int x, int y, int width, int height
) implements PawnboundPlayPacket {
    public static final StreamCodec<ByteBuf, PawnboundParentWindowPacket> CODEC =
            StreamCodec.composite(
                    NativeWindowHandle.STREAM_CODEC,
                    PawnboundParentWindowPacket::parentWindowHandle,
                    ByteBufCodecs.INT,
                    PawnboundParentWindowPacket::x,
                    ByteBufCodecs.INT,
                    PawnboundParentWindowPacket::y,
                    ByteBufCodecs.INT,
                    PawnboundParentWindowPacket::width,
                    ByteBufCodecs.INT,
                    PawnboundParentWindowPacket::height,

                    PawnboundParentWindowPacket::new
            );
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
