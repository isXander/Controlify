package dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.host.ipc.ControllerPlayPacketListener;
import dev.isxander.controlify.splitscreen.ipc.utils.ExtraStreamCodecs;
import dev.isxander.controlify.splitscreen.window.manager.NativeWindowHandle;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ControllerboundHelloPacket(@Nullable ControllerUID controller) implements ControllerboundPlayPacket {
    public static final StreamCodec<ByteBuf, ControllerboundHelloPacket> CODEC =
            ByteBufCodecs.optional(ExtraStreamCodecs.CONTROLLER_UID)
                    .map(
                            opt -> new ControllerboundHelloPacket(opt.orElse(null)),
                            packet -> Optional.ofNullable(packet.controller())
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
