package dev.isxander.controlify.platform.network;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public interface S2CNetworkApi extends SidedNetworkApi {
    <T> void sendPacket(ServerPlayer recipient, Identifier channel, T packet);

    <T> void listenForPacket(Identifier channel, PacketListener<T> listener);

    @FunctionalInterface
    interface PacketListener<T> {
        void listen(T packet);
    }
}
