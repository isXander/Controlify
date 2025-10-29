package dev.isxander.controlify.config;

import com.mojang.serialization.Codec;

import java.util.Optional;
import java.util.stream.Stream;

class EmptyValueInput implements ValueInput {
    public static final EmptyValueInput INSTANCE = new EmptyValueInput();

    @Override
    public <T> Optional<T> read(String key, Codec<T> codec) {
        return Optional.empty();
    }

    @Override
    public Optional<ValueInput> childObject(String key) {
        return Optional.empty();
    }

    @Override
    public ValueInput childObjectOrEmpty(String key) {
        return INSTANCE;
    }

    @Override
    public <T> Optional<List<T>> childList(String key, Codec<T> codec) {
        return Optional.empty();
    }

    @Override
    public <T> List<T> childListOrEmpty(String key, Codec<T> codec) {
        return null;
    }

    static class EmptyList<T> implements ValueInput.List<T> {
        @Override
        public Stream<T> stream() {
            return Stream.empty();
        }
    }
}
