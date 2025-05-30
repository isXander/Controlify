package dev.isxander.splitscreen.client.engine;

import dev.isxander.splitscreen.client.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.splitscreen.client.ipc.packets.pawnbound.play.PawnboundEngineCustomPayloadPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * SplitscreenEngine is an abstraction to allow different implementations of the splitscreen.
 * The only currently implemented one is the window reparenting.
 */
public interface SplitscreenEngine {
    StreamCodec<FriendlyByteBuf, ControllerboundEngineCustomPayloadPacket> getControllerboundCustomPayloadCodec();

    StreamCodec<FriendlyByteBuf, PawnboundEngineCustomPayloadPacket> getPawnboundCustomPayloadCodec();
}
