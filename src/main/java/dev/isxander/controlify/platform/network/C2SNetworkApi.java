package dev.isxander.controlify.platform.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface C2SNetworkApi extends SidedNetworkApi {
    <T> void sendPacket(ResourceLocation channel, T packet);

    <T> void listenForPacket(ResourceLocation channel, PacketListener<T> listener);

    @FunctionalInterface
    interface PacketListener<T> {
        void listen(T packet, ServerPlayer sender);
    }
}
