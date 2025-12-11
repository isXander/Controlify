package dev.isxander.controlify.bindings.input;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.Identifier;

import java.util.List;

public record AxisInput(Identifier axis) implements Input {
    public static final String INPUT_ID = "axis";

    public static final MapCodec<AxisInput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf(INPUT_ID).forGetter(AxisInput::axis)
    ).apply(instance, AxisInput::new));

    @Override
    public float state(ControllerStateView state) {
        return state.getAxisState(axis);
    }

    @Override
    public List<Identifier> getRelevantInputs() {
        return List.of(axis);
    }

    @Override
    public InputType<?> type() {
        return InputType.AXIS;
    }
}
