package dev.isxander.controlify.platform.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface C2SNetworkApi extends SidedNetworkApi {
    <T> void sendPacket(ResourceLocation channel, T packet);

    <T> CustomPacketPayload createPayload(ResourceLocation channel, T packet);

    <T> void listenForPacket(ResourceLocation channel, PacketListener<T> listener);

    @FunctionalInterface
    interface PacketListener<T> {
        void listen(T packet, ServerPlayer sender, PacketSender responseSender);
    }
}
