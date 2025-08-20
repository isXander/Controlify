package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface SerializableGesture<T extends SerializableGesture<T>> extends Gesture {

    MapCodec<SerializableGesture<?>> MAP_CODEC = GestureType.createCodec(GestureType.TYPES, GestureType::mapCodec, SerializableGesture::type, "type");
    Codec<SerializableGesture<?>> CODEC = MAP_CODEC.codec();

    GestureType<T> type();

}
