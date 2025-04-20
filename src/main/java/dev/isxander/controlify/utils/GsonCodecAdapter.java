package dev.isxander.controlify.utils;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.lang.reflect.Type;

/**
 * Allows codecs to be used as Json(De)Serializers for GSON usage.
 * @param codec codec to use
 * @param <T> type to encode/decode
 */
public record GsonCodecAdapter<T>(Codec<T> codec) implements JsonSerializer<T>, JsonDeserializer<T> {
    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return codec.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return codec.parse(JsonOps.INSTANCE, json).getOrThrow();
    }
}
