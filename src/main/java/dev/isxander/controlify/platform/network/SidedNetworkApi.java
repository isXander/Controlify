package dev.isxander.controlify.platform.network;

import dev.isxander.controlify.platform.main.PlatformMainUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public interface SidedNetworkApi {
	static C2SNetworkApi C2S() {
        return PlatformMainUtil.c2sNetworkApi();
    }

    static S2CNetworkApi S2C() {
        return PlatformMainUtil.s2CNetworkApi();
    }

    <T> void registerPacket(Identifier channel, StreamCodec<FriendlyByteBuf, T> handler);
}
