//? if neoforge {
/*package dev.isxander.controlify.platform.network.neoforge;

import dev.isxander.controlify.platform.network.C2SNetworkApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;

public class C2SNetworkApiNeoforge implements C2SNetworkApi {
    public static final C2SNetworkApiNeoforge INSTANCE = new C2SNetworkApiNeoforge();

    @Override
    public <T> void sendPacket(Identifier channel, T packet) {
        // TODO
    }

    @Override
    public <T> CustomPacketPayload createPayload(Identifier channel, T packet) {
        return null;
    }

    @Override
    public <T> void listenForPacket(Identifier channel, PacketListener<T> listener) {
        // TODO
    }

    @Override
    public <T> void registerPacket(Identifier channel, StreamCodec<FriendlyByteBuf, T> handler) {
        // TODO
    }

    private IEventBus getModEventBus() {
        return ModLoadingContext.get().getActiveContainer().getEventBus();
    }
}
*///?}
