package dev.isxander.controlify.bindings.v2.input;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record AxisInput(ResourceLocation axis) implements Input {
    public static final String INPUT_ID = "axis";

    public static final MapCodec<AxisInput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf(INPUT_ID).forGetter(AxisInput::axis)
    ).apply(instance, AxisInput::new));

    @Override
    public float state(ControllerStateView state) {
        return state.getAxisState(axis);
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of(axis);
    }

    @Override
    public InputType<?> type() {
        return InputType.AXIS;
    }
}
