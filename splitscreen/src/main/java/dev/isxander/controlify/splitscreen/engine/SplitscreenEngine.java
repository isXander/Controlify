package dev.isxander.controlify.splitscreen.engine;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play.PawnboundEngineCustomPayloadPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface SplitscreenEngine {
    StreamCodec<FriendlyByteBuf, ControllerboundEngineCustomPayloadPacket> getControllerboundCustomPayloadCodec();

    StreamCodec<FriendlyByteBuf, PawnboundEngineCustomPayloadPacket> getPawnboundCustomPayloadCodec();
}
