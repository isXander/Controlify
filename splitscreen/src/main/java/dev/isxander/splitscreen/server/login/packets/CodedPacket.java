package dev.isxander.splitscreen.server.login.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * The login phase of Minecraft networking does not support
 * {@link net.minecraft.network.protocol.common.custom.CustomPacketPayload}
 * so this is an easy way to make OOP record packets.
 * @param <T> <code>this</code> - the type of the child class that the codec deserializes
 */
public interface CodedPacket<T> {
    StreamCodec<FriendlyByteBuf, T> codec();

    default FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        //noinspection unchecked
        codec().encode(buf, (T) this);
        return buf;
    }
}
