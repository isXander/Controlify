package dev.isxander.splitscreen.engine;

import dev.isxander.splitscreen.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.splitscreen.ipc.packets.pawnbound.play.PawnboundEngineCustomPayloadPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface SplitscreenEngine {
    StreamCodec<FriendlyByteBuf, ControllerboundEngineCustomPayloadPacket> getControllerboundCustomPayloadCodec();

    StreamCodec<FriendlyByteBuf, PawnboundEngineCustomPayloadPacket> getPawnboundCustomPayloadCodec();
}
