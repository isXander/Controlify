package dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play;

import dev.isxander.controlify.splitscreen.host.ipc.ControllerPlayPacketListener;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ControllerboundEngineCustomPayloadPacket(CustomPacketPayload payload) implements ControllerboundPlayPacket {
    public static final PacketType<ControllerboundEngineCustomPayloadPacket> TYPE =
            ControllerboundPlayPacket.createType("engine_custom_payload");

    @Override
    public void handle(ControllerPlayPacketListener handler) {
        handler.handleEngineCustomPayload(this);
    }

    @Override
    public PacketType<ControllerboundEngineCustomPayloadPacket> type() {
        return TYPE;
    }
}
