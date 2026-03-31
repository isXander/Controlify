package dev.isxander.controlify.platform.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public interface SidedNetworkApi {
    static C2SNetworkApi C2S() {
        //? if fabric
        return dev.isxander.controlify.platform.network.fabric.C2SNetworkApiFabric.INSTANCE;
        //? if neoforge
        //return dev.isxander.controlify.platform.network.neoforge.C2SNetworkApiNeoforge.INSTANCE;
    }

    static S2CNetworkApi S2C() {
        //? if fabric
        return dev.isxander.controlify.platform.network.fabric.S2CNetworkApiFabric.INSTANCE;
        //? if neoforge
        //return dev.isxander.controlify.platform.network.neoforge.S2CNetworkApiNeoforge.INSTANCE;
    }

    <T> void registerPacket(Identifier channel, StreamCodec<FriendlyByteBuf, T> handler);
}
