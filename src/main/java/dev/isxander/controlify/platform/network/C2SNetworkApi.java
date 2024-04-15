package dev.isxander.controlify.platform.network;

import net.minecraft.resources.ResourceLocation;

public interface C2SNetworkApi extends SidedNetworkApi {
    <T> void sendPacket(ResourceLocation channel, T packet);
}
