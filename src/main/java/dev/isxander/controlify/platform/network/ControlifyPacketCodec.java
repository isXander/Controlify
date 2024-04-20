package dev.isxander.controlify.platform.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ControlifyPacketCodec<T> {
    static <T> ControlifyPacketCodec<T> of(BiConsumer<FriendlyByteBuf, T> encoder, Function<FriendlyByteBuf, T> decoder) {
        return new ControlifyPacketCodec<>() {
            @Override
            public void encode(FriendlyByteBuf buf, T packet) {
                encoder.accept(buf, packet);
            }

            @Override
            public T decode(FriendlyByteBuf buf) {
                return decoder.apply(buf);
            }
        };
    }

    void encode(FriendlyByteBuf buf, T packet);

    T decode(FriendlyByteBuf buf);
}
