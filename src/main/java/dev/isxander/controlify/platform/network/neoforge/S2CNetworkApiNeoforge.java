package dev.isxander.controlify.platform.network.neoforge;

import dev.isxander.controlify.platform.network.S2CNetworkApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class S2CNetworkApiNeoforge implements S2CNetworkApi {
    public static final S2CNetworkApiNeoforge INSTANCE = new S2CNetworkApiNeoforge();

    @Override
    public <T> void sendPacket(ServerPlayer recipient, Identifier channel, T packet) {
        // TODO
    }

    @Override
    public <T> void listenForPacket(Identifier channel, PacketListener<T> listener) {
        // TODO
    }

    @Override
    public <T> void registerPacket(Identifier channel, StreamCodec<FriendlyByteBuf, T> handler) {
        // TODO
    }
}
