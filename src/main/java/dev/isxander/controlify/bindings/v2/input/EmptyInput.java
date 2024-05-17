package dev.isxander.controlify.bindings.v2.input;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record EmptyInput() implements Input {
    public static final EmptyInput INSTANCE = new EmptyInput();

    public static final String INPUT_ID = "empty";
    public static final MapCodec<EmptyInput> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public float state(ControllerStateView state) {
        return 0;
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of();
    }

    @Override
    public InputType<?> type() {
        return InputType.EMPTY;
    }

    public static boolean equals(Input input) {
        return input instanceof EmptyInput;
    }
}
