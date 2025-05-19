package dev.isxander.controlify.splitscreen.server.login.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface CodedPacket<T> {
    StreamCodec<FriendlyByteBuf, T> codec();

    default FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        //noinspection unchecked
        codec().encode(buf, (T) this);
        return buf;
    }
}
