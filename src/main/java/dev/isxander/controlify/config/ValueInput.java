package dev.isxander.controlify.config;

import com.mojang.serialization.Codec;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ValueInput {
    <T> Optional<T> read(String key, Codec<T> codec);

    default <T> T readOr(String key, Codec<T> codec, T defaultValue) {
        return read(key, codec).orElse(defaultValue);
    }

    default Optional<Boolean> readBoolean(String key) {
        return read(key, Codec.BOOL);
    }
    default boolean readBooleanOr(String key, boolean defaultValue) {
        return readBoolean(key).orElse(defaultValue);
    }
    default Optional<Byte> readByte(String key) {
        return read(key, Codec.BYTE);
    }
    default byte readByteOr(String key, byte defaultValue) {
        return readByte(key).orElse(defaultValue);
    }
    default Optional<Short> readShort(String key) {
        return read(key, Codec.SHORT);
    }
    default short readShortOr(String key, short defaultValue) {
        return readShort(key).orElse(defaultValue);
    }
    default Optional<Integer> readInt(String key) {
        return read(key, Codec.INT);
    }
    default int readIntOr(String key, int defaultValue) {
        return readInt(key).orElse(defaultValue);
    }
    default Optional<Long> readLong(String key) {
        return read(key, Codec.LONG);
    }
    default long readLongOr(String key, long defaultValue) {
        return readLong(key).orElse(defaultValue);
    }
    default Optional<Float> readFloat(String key) {
        return read(key, Codec.FLOAT);
    }
    default float readFloatOr(String key, float defaultValue) {
        return readFloat(key).orElse(defaultValue);
    }
    default Optional<Double> readDouble(String key) {
        return read(key, Codec.DOUBLE);
    }
    default double readDoubleOr(String key, double defaultValue) {
        return readDouble(key).orElse(defaultValue);
    }
    default Optional<String> readString(String key) {
        return read(key, Codec.STRING);
    }
    default String readStringOr(String key, String defaultValue) {
        return readString(key).orElse(defaultValue);
    }

    Optional<ValueInput> childObject(String key);
    default ValueInput childObjectOrEmpty(String key) {
        return childObject(key).orElse(EmptyValueInput.INSTANCE);
    }
    default void readObject(String key, Consumer<ValueInput> childInput) {
        childObject(key).ifPresent(childInput);
    }

    <T> Optional<List<T>> childList(String key, Codec<T> codec);
    default <T> List<T> childListOrEmpty(String key, Codec<T> codec) {
        return childList(key, codec).orElse(new EmptyValueInput.EmptyList<>());
    }

    interface List<T> {
        Stream<T> stream();
    }
}
