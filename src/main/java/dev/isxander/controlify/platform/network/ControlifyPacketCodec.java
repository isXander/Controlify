package dev.isxander.controlify.platform.network;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ControlifyPacketCodec<T>
    //? if >=1.20.5
    extends net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, T>
{
    static <T> ControlifyPacketCodec<T> of(BiConsumer<FriendlyByteBuf, T> encoder, Function<FriendlyByteBuf, T> decoder) {
        return new ControlifyPacketCodec<>() {
            @Override
            public void encode(@NotNull FriendlyByteBuf buf, @NotNull T packet) {
                encoder.accept(buf, packet);
            }

            @Override
            public @NotNull T decode(@NotNull FriendlyByteBuf buf) {
                return decoder.apply(buf);
            }
        };
    }

    void encode(@NotNull FriendlyByteBuf buf, @NotNull T packet);

    @NotNull T decode(@NotNull FriendlyByteBuf buf);
}
