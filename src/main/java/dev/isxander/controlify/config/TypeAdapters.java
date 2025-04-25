package dev.isxander.controlify.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.function.Function;

public final class TypeAdapters {
    public static class ClassTypeAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return Class.forName(json.getAsString());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

    public static class StringDerivedTypeAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
        private final Function<String, T> fromString;
        private final Function<T, String> toString;

        public StringDerivedTypeAdapter(Function<String, T> fromString, Function<T, String> toString) {
            this.fromString = fromString;
            this.toString = toString;
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return fromString.apply(json.getAsString());
        }

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(toString.apply(src));
        }
    }
}
