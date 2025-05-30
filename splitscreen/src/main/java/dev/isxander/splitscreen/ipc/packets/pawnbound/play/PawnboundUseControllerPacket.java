package dev.isxander.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.ipc.utils.ExtraStreamCodecs;
import dev.isxander.splitscreen.remote.ipc.PawnPlayPacketListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record PawnboundUseControllerPacket(ControllerUID controllerUID) implements PawnboundPlayPacket {
    public static final StreamCodec<ByteBuf, PawnboundUseControllerPacket> CODEC =
            ExtraStreamCodecs.CONTROLLER_UID.map(PawnboundUseControllerPacket::new, PawnboundUseControllerPacket::controllerUID);
    public static final PacketType<PawnboundUseControllerPacket> TYPE = PawnboundPlayPacket.createType("use_controller");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleUseController(this);
    }

    @Override
    public PacketType<PawnboundUseControllerPacket> type() {
        return TYPE;
    }
}
