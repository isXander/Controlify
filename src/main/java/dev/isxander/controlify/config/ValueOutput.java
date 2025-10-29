package dev.isxander.controlify.config;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface ValueOutput {
    <T> void put(String field, Codec<T> codec, T value);

    default <T> void putNullable(String field, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            put(field, codec, value);
        }
    }

    default void putBoolean(String key, boolean value) {
        put(key, Codec.BOOL, value);
    }
    default void putByte(String key, byte value) {
        put(key, Codec.BYTE, value);
    }
    default void putShort(String key, short value) {
        put(key, Codec.SHORT, value);
    }
    default void putInt(String key, int value) {
        put(key, Codec.INT, value);
    }
    default void putLong(String key, long value) {
        put(key, Codec.LONG, value);
    }
    default void putFloat(String key, float value) {
        put(key, Codec.FLOAT, value);
    }
    default void putDouble(String key, double value) {
        put(key, Codec.DOUBLE, value);
    }
    default void putString(String key, String value) {
        put(key, Codec.STRING, value);
    }

    ValueOutput childObject(String key);
    default void putObject(String key, Consumer<ValueOutput> childOutput) {
        childOutput.accept(childObject(key));
    }

    <T> ValueOutput.List<T> childList(String key, Codec<T> codec);

    interface List<T> {
        void add(T value);
    }
}
