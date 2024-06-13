//? if fabric {
package dev.isxander.controlify.platform.network.fabric;

import dev.isxander.controlify.platform.network.C2SNetworkApi;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class C2SNetworkApiFabric implements C2SNetworkApi {
    public static final C2SNetworkApiFabric INSTANCE = new C2SNetworkApiFabric();

    private final Map<ResourceLocation, FabricPacketWrapper<?>> packets = new HashMap<>();

    private C2SNetworkApiFabric() {
    }

    @Override
    public <T> void registerPacket(ResourceLocation channel, ControlifyPacketCodec<T> codec) {
        packets.put(channel, new FabricPacketWrapper<>(channel, codec/*? if >1.20.4 {*/, net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S()/*?}*/));
    }

    @Override
    public <T> void sendPacket(ResourceLocation channel, T packet) {
        FabricPacketWrapper<T> packetWrapper = getWrapper(channel);
        ClientPlayNetworking.send(packetWrapper.new FabricPacketPayloadWrapper(packet));
    }

    @Override
    public <T> void listenForPacket(ResourceLocation channel, PacketListener<T> listener) {
        FabricPacketWrapper<T> packetWrapper = getWrapper(channel);

        /*? if >1.20.4 {*/
        ServerPlayNetworking.registerGlobalReceiver(packetWrapper.type, (packet, context) -> {
            listener.listen(packet.payload, context.player());
        });
        /*?} else {*/
        /*ServerPlayNetworking.registerGlobalReceiver(packetWrapper.type, (packet, player, responseSender) -> {
            listener.listen(packet.payload, player);
        });
        *//*?}*/
    }

    private <T> FabricPacketWrapper<T> getWrapper(ResourceLocation channel) {
        return (FabricPacketWrapper<T>) packets.get(channel);
    }
}
//?}
