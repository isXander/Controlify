package dev.isxander.controlify.platform.network;

import dev.isxander.controlify.platform.network.impl.C2SNetworkApiImpl;
import dev.isxander.controlify.platform.network.impl.S2CNetworkApiImpl;
import net.minecraft.resources.ResourceLocation;

public interface SidedNetworkApi {
    static C2SNetworkApi C2S() {
        return C2SNetworkApiImpl.INSTANCE;
    }

    static S2CNetworkApi S2C() {
        return S2CNetworkApiImpl.INSTANCE;
    }

    <T> void registerPacket(ResourceLocation channel, ControlifyPacketCodec<T> handler);
}
