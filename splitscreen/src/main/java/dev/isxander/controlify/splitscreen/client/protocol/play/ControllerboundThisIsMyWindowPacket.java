package dev.isxander.controlify.splitscreen.client.protocol.play;

import dev.isxander.controlify.splitscreen.server.protocol.play.ControllerPlayPacketListener;
import dev.isxander.controlify.splitscreen.window.embedder.NativeWindowHandle;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundThisIsMyWindowPacket(NativeWindowHandle nativeWindowHandle) implements ControllerboundPlayPacket {
    public static final StreamCodec<ByteBuf, ControllerboundThisIsMyWindowPacket> CODEC =
            ByteBufCodecs.LONG.map(
                    handle -> new ControllerboundThisIsMyWindowPacket(new NativeWindowHandle(handle)),
                    packet -> packet.nativeWindowHandle().handle()
            );
    public static final PacketType<ControllerboundThisIsMyWindowPacket> TYPE =
            ControllerboundPlayPacket.createType("this_is_my_window");

    @Override
    public void handle(ControllerPlayPacketListener handler) {
        handler.handleThisIsMyWindow(this);
    }

    @Override
    public PacketType<? extends Packet<ControllerPlayPacketListener>> type() {
        return TYPE;
    }
}
