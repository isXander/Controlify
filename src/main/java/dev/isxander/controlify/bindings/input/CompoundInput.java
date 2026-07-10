package dev.isxander.controlify.bindings.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.Identifier;

import java.util.List;

public record CompoundInput(List<Input> inputs) implements Input {
    public static final String INPUT_ID = "compound";

    public static final MapCodec<CompoundInput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.lazyInitialized(()-> Input.CODEC).listOf().fieldOf(INPUT_ID).forGetter(CompoundInput::inputs)
    ).apply(instance, CompoundInput::new));


    @Override
    public float state(ControllerStateView state) {
        for (Input input : inputs) {
            if (input.state(state) == 0) return 0;
        }
        return 1;
    }

    @Override
    public List<Identifier> getRelevantInputs() {
        return inputs.stream().flatMap(input -> input.getRelevantInputs().stream()).toList();
    }

    @Override
    public InputType<?> type() {
        return InputType.COMPOUND;
    }
}
