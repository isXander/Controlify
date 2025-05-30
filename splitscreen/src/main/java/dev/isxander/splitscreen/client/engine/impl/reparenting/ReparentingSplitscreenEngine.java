package dev.isxander.splitscreen.client.engine.impl.reparenting;

import dev.isxander.splitscreen.client.engine.SplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ipc.ControllerboundTakeFocusPayload;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ipc.ControllerboundThisIsMyWindowPayload;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ipc.PawnboundSetWindowActivePayload;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ipc.PawnboundThrottleFrameratePayload;
import dev.isxander.splitscreen.client.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.splitscreen.client.ipc.packets.pawnbound.play.PawnboundEngineCustomPayloadPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;

import java.util.List;

public abstract class ReparentingSplitscreenEngine implements SplitscreenEngine {
    private static final StreamCodec<FriendlyByteBuf, ControllerboundEngineCustomPayloadPacket> CONTROLLERBOUND_CODEC =
            CustomPacketPayload.codec(
                    id -> DiscardedPayload.codec(id, 1048576),
                    List.of(
                            new CustomPacketPayload.TypeAndCodec<>(ControllerboundThisIsMyWindowPayload.TYPE, ControllerboundThisIsMyWindowPayload.CODEC),
                            new CustomPacketPayload.TypeAndCodec<>(ControllerboundTakeFocusPayload.TYPE, ControllerboundTakeFocusPayload.STREAM_CODEC)
                    )
            ).map(ControllerboundEngineCustomPayloadPacket::new, ControllerboundEngineCustomPayloadPacket::payload);

    @Override
    public StreamCodec<FriendlyByteBuf, ControllerboundEngineCustomPayloadPacket> getControllerboundCustomPayloadCodec() {
        return CONTROLLERBOUND_CODEC;
    }

    private static final StreamCodec<FriendlyByteBuf, PawnboundEngineCustomPayloadPacket> PAWNBOUND_CODEC =
            CustomPacketPayload.codec(
                    id -> DiscardedPayload.codec(id, 1048576),
                    List.of(
                            new CustomPacketPayload.TypeAndCodec<>(PawnboundSetWindowActivePayload.TYPE, PawnboundSetWindowActivePayload.CODEC),
                            new CustomPacketPayload.TypeAndCodec<>(PawnboundThrottleFrameratePayload.TYPE, PawnboundThrottleFrameratePayload.CODEC)
                    )
            ).map(PawnboundEngineCustomPayloadPacket::new, PawnboundEngineCustomPayloadPacket::payload);

    @Override
    public StreamCodec<FriendlyByteBuf, PawnboundEngineCustomPayloadPacket> getPawnboundCustomPayloadCodec() {
        return PAWNBOUND_CODEC;
    }
}
