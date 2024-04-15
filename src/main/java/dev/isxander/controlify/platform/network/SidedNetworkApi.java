package dev.isxander.controlify.platform.network;

import dev.isxander.controlify.platform.network.impl.ClientNetworkApi;
import dev.isxander.controlify.platform.network.impl.ServerNetworkApi;
import net.minecraft.resources.ResourceLocation;

public interface SidedNetworkApi {
    static C2SNetworkApi C2S() {
        return ClientNetworkApi.INSTANCE;
    }

    static S2CNetworkApi S2C() {
        return ServerNetworkApi.INSTANCE;
    }

    <T> void registerPacket(ResourceLocation channel, ControlifyPacketCodec<T> handler);

    <T> void listenForPacket(ResourceLocation channel, PacketListener<T> listener);
}
