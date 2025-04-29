package dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PawnboundEngineCustomPayloadPacket(
        CustomPacketPayload payload) implements PawnboundPlayPacket {
    public static final PacketType<PawnboundEngineCustomPayloadPacket> TYPE =
            PawnboundPlayPacket.createType("engine_custom_payload");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleEngineCustomPayload(this);
    }

    @Override
    public PacketType<PawnboundEngineCustomPayloadPacket> type() {
        return TYPE;
    }
}
