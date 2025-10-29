package dev.isxander.controlify.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.*;
import net.minecraft.util.ProblemReporter;
import org.jetbrains.annotations.NotNull;

public class JsonValueOutput implements ValueOutput {
    private final DynamicOps<JsonElement> ops;
    private JsonObject output;
    private final ProblemReporter problemReporter;

    public JsonValueOutput(DynamicOps<JsonElement> ops, JsonObject object, ProblemReporter problemReporter) {
        this.ops = ops;
        this.output = object;
        this.problemReporter = problemReporter;
    }

    @Override
    public <T> void put(String key, Codec<T> codec, T value) {
        switch (codec.encodeStart(this.ops, value)) {
            case DataResult.Success<JsonElement> success -> {
                this.output.add(key, success.value());
            }

            case DataResult.Error<JsonElement> error -> {
                this.problemReporter.report(new EncodeToFieldFailedProblem(key, value, error));
                error.partialValue().ifPresent(o -> this.output.add(key, o));
            }
        }
    }

    @Override
    public ValueOutput childObject(String key) {
        var childObject = new JsonObject();
        this.output.add(key, childObject);
        return new JsonValueOutput(this.ops, childObject, this.reporterForChild(key));
    }

    @Override
    public <T> List<T> childList(String key, Codec<T> codec) {
        var childArray = new JsonArray();
        this.output.add(key, childArray);
        return new ListImpl<>(codec, this.ops, childArray, this.problemReporter);
    }

    public JsonObject buildResult() {
        return this.output;
    }

    private ProblemReporter reporterForChild(String name) {
        return this.problemReporter.forChild(new ProblemReporter.FieldPathElement(name));
    }

    public record EncodeToFieldFailedProblem(String name, Object value, DataResult.Error<?> error) implements ProblemReporter.Problem {
        @Override
        public @NotNull String description() {
            return "Failed to encode value '" + this.value + "' to field '" + this.name + "': " + this.error.message();
        }
    }

    private static class ListImpl<T> implements ValueOutput.List<T> {
        private final DynamicOps<JsonElement> ops;
        private final Codec<T> codec;
        private final JsonArray output;
        private final ProblemReporter problemReporter;

        public ListImpl(Codec<T> codec, DynamicOps<JsonElement> ops, JsonArray output, ProblemReporter problemReporter) {
            this.codec = codec;
            this.ops = ops;
            this.output = output;
            this.problemReporter = problemReporter;
        }

        @Override
        public void add(T element) {
            switch (codec.encodeStart(this.ops, element)) {
                case DataResult.Success<JsonElement> success ->
                        this.output.add(success.value());
                case DataResult.Error<JsonElement> error -> {
                    this.problemReporter.report(new EncodeToFieldFailedProblem("list element", element, error));
                    error.partialValue().ifPresent(this.output::add);
                }
            }
        }
    }
}
