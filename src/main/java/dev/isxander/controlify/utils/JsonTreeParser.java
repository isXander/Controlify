package dev.isxander.controlify.utils;

import com.google.gson.*;
import org.quiltmc.parsers.json.JsonReader;

import java.io.IOException;
import java.util.Optional;

/**
 * An alternative to GSON that purely serializes to a tree structure.
 * It is required because GSON does not correctly use the parser's `readInt` method and fails when using hexadecimal.
 */
public final class JsonTreeParser {
    public static JsonElement parse(JsonReader reader) throws IOException {
        switch (reader.peek()) {
            case BEGIN_OBJECT -> {
                reader.beginObject();
                var object = new JsonObject();
                while (reader.hasNext()) {
                    var name = reader.nextName();
                    var value = parse(reader);
                    object.add(name, value);
                }
                reader.endObject();
                return object;
            }
            case BEGIN_ARRAY -> {
                reader.beginArray();
                var array = new JsonArray();
                while (reader.hasNext()) {
                    var value = parse(reader);
                    array.add(value);
                }
                reader.endArray();
                return array;
            }
            case STRING -> {
                return new JsonPrimitive(reader.nextString());
            }
            case NUMBER -> {
                // try to parse as increasingly more easily passable number types in order
                // to get the smallest possible type. for example, all doubles can also be longs
                Number number = tryParseNumber(reader::nextInt)
                        .or(() -> tryParseNumber(reader::nextLong))
                        .or(() -> tryParseNumber(reader::nextDouble))
                        .orElseThrow();

                return new JsonPrimitive(number);
            }
            case BOOLEAN -> {
                return new JsonPrimitive(reader.nextBoolean());
            }
            case NULL -> {
                reader.nextNull();
                return JsonNull.INSTANCE;
            }
            default -> throw new JsonParseException("Unexpected token: " + reader.peek());
        }
    }

    private static Optional<Number> tryParseNumber(NumberParser parser) {
        try {
            return Optional.of(parser.parse());
        } catch (NumberFormatException | IOException e) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface NumberParser {
        Number parse() throws NumberFormatException, IOException;
    }
}
