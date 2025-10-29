package dev.isxander.controlify.config;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ProblemReporter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class JsonValueInput implements ValueInput {
    private final DynamicOps<JsonElement> ops;
    private final JsonObject input;
    private final ProblemReporter problemReporter;

    public JsonValueInput(DynamicOps<JsonElement> ops, JsonObject object, ProblemReporter problemReporter) {
        this.ops = ops;
        this.input = object;
        this.problemReporter = problemReporter;
    }

    @Override
    public <T> Optional<T> read(String key, Codec<T> codec) {
        JsonElement element = this.input.get(key);
        if (element == null) {
            return Optional.empty();
        }

        return switch (codec.parse(this.ops, element)) {
            case DataResult.Success<T> success -> Optional.of(success.value());
            case DataResult.Error<T> error -> {
                this.problemReporter.report(new DecodeFromFieldFailedProblem(key, element, error));
                yield error.partialValue();
            }
        };
    }

    @Override
    public Optional<ValueInput> childObject(String key) {
        JsonObject child = this.input.getAsJsonObject(key);
        return child != null
                ? Optional.of(new JsonValueInput(this.ops, child, this.reporterForChild(key)))
                : Optional.empty();
    }

    @Override
    public <T> Optional<List<T>> childList(String key, Codec<T> codec) {
        JsonArray child = this.input.getAsJsonArray(key);
        return child != null
                ? Optional.of(new ListImpl<>(codec, this.ops, child, this.reporterForChild(key)))
                : Optional.empty();
    }


    private ProblemReporter reporterForChild(String name) {
        return this.problemReporter.forChild(new ProblemReporter.FieldPathElement(name));
    }

    private static class ListImpl<T> implements List<T> {
        private final Codec<T> codec;
        private final DynamicOps<JsonElement> ops;
        private final JsonArray array;
        private final ProblemReporter problemReporter;

        public ListImpl(Codec<T> codec, DynamicOps<JsonElement> ops, JsonArray array, ProblemReporter problemReporter) {
            this.codec = codec;
            this.ops = ops;
            this.array = array;
            this.problemReporter = problemReporter;
        }


        @Override
        public Stream<T> stream() {
            return Streams.mapWithIndex(this.array.asList().stream(), (element, i) -> {
                return switch (this.codec.parse(this.ops, element)) {
                    case DataResult.Success<T> success -> success.value();
                    case DataResult.Error<T> error -> {
                        this.problemReporter.report(new DecodeFromFieldFailedProblem("[" + i + "]", element, error));
                        yield error.partialValue().orElse(null);
                    }
                };
            }).filter(Objects::nonNull);
        }
    }

    public record DecodeFromFieldFailedProblem(String name, JsonElement tag, DataResult.Error<?> error) implements ProblemReporter.Problem {
        @Override
        public @NotNull String description() {
            return "Failed to decode value '" + this.tag + "' from field '" + this.name + "': " + this.error.message();
        }
    }
}
