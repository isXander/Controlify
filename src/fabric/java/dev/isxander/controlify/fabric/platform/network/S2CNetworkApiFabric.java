package dev.isxander.controlify.fabric.platform.network;

import dev.isxander.controlify.platform.network.S2CNetworkApi;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public final class S2CNetworkApiFabric implements S2CNetworkApi {
    private final Map<Identifier, FabricPacketWrapper<?>> packets = new HashMap<>();

    @Override
    public <T> void registerPacket(Identifier channel, StreamCodec<FriendlyByteBuf, T> codec) {
        packets.put(channel, new FabricPacketWrapper<>(channel, codec, PayloadTypeRegistry.clientboundPlay()));
    }

    @Override
    public <T> void sendPacket(ServerPlayer recipient, Identifier channel, T packet) {
        FabricPacketWrapper<T> packetWrapper = getWrapper(channel);
        ServerPlayNetworking.send(recipient, packetWrapper.new FabricPacketPayloadWrapper(packet));
    }

    @Override
    public <T> void listenForPacket(Identifier channel, PacketListener<T> listener) {
        FabricPacketWrapper<T> packetWrapper = getWrapper(channel);

        ClientPlayNetworking.registerGlobalReceiver(packetWrapper.type, (packet, context) -> {
            listener.listen(packet.payload);
        });
    }

    private <T> FabricPacketWrapper<T> getWrapper(Identifier channel) {
        return (FabricPacketWrapper<T>) packets.get(channel);
    }
}
