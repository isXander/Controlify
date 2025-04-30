package dev.isxander.controlify.splitscreen.engine.impl.fboshare;

import dev.isxander.controlify.splitscreen.engine.SplitscreenEngine;
import dev.isxander.controlify.splitscreen.engine.impl.fboshare.ipc.ControllerboundShareMemoryPayload;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play.PawnboundEngineCustomPayloadPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;

import java.util.List;

public abstract class FboshareSplitscreenEngine implements SplitscreenEngine {
    private static final StreamCodec<FriendlyByteBuf, ControllerboundEngineCustomPayloadPacket> CONTROLLERBOUND_CODEC =
            CustomPacketPayload.codec(
                    id -> DiscardedPayload.codec(id, 1048576),
                    List.of(
                            new CustomPacketPayload.TypeAndCodec<>(ControllerboundShareMemoryPayload.TYPE, ControllerboundShareMemoryPayload.CODEC)
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

                    )
            ).map(PawnboundEngineCustomPayloadPacket::new, PawnboundEngineCustomPayloadPacket::payload);

    @Override
    public StreamCodec<FriendlyByteBuf, PawnboundEngineCustomPayloadPacket> getPawnboundCustomPayloadCodec() {
        return PAWNBOUND_CODEC;
    }
}
