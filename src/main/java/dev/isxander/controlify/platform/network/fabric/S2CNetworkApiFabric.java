//? if fabric {
package dev.isxander.controlify.platform.network.fabric;

import dev.isxander.controlify.platform.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public final class S2CNetworkApiFabric implements S2CNetworkApi {
    public static final S2CNetworkApiFabric INSTANCE = new S2CNetworkApiFabric();

    private final Map<ResourceLocation, FabricPacketWrapper<?>> packets = new HashMap<>();

    private S2CNetworkApiFabric() {
    }

    @Override
    public <T> void registerPacket(ResourceLocation channel, StreamCodec<FriendlyByteBuf, T> codec) {
        packets.put(channel, new FabricPacketWrapper<>(channel, codec, PayloadTypeRegistry.playS2C()));
    }

    @Override
    public <T> void sendPacket(ServerPlayer recipient, ResourceLocation channel, T packet) {
        FabricPacketWrapper<T> packetWrapper = getWrapper(channel);
        ServerPlayNetworking.send(recipient, packetWrapper.new FabricPacketPayloadWrapper(packet));
    }

    @Override
    public <T> void listenForPacket(ResourceLocation channel, PacketListener<T> listener) {
        FabricPacketWrapper<T> packetWrapper = getWrapper(channel);

        ClientPlayNetworking.registerGlobalReceiver(packetWrapper.type, (packet, context) -> {
            listener.listen(packet.payload);
        });
    }

    private <T> FabricPacketWrapper<T> getWrapper(ResourceLocation channel) {
        return (FabricPacketWrapper<T>) packets.get(channel);
    }
}
//?}
