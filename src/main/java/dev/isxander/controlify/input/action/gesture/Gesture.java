package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface Gesture {
    MapCodec<Gesture> MAP_CODEC = SerializableGesture.MAP_CODEC
            .flatXmap(
                    DataResult::success,
                    g -> g instanceof SerializableGesture<?> s
                            ? DataResult.success(s)
                            : DataResult.error(() -> "Gesture is not a SerializableGesture: " + g.getClass().getName())
            );
    Codec<Gesture> CODEC = MAP_CODEC.codec();

    boolean supports(ChannelKind channel);

    void onSignal(Signal signal, Accumulator acc);

    String describe();

    Set<ResourceLocation> monitoredInputs();
}
