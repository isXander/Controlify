package dev.isxander.controlify.bindings.input;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ButtonInput(Identifier button) implements Input {
    public static final String INPUT_ID = "button";

    public static final MapCodec<ButtonInput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf(INPUT_ID).forGetter(ButtonInput::button)
    ).apply(instance, ButtonInput::new));

    @Override
    public float state(ControllerStateView state) {
        return state.isButtonDown(button) ? 1 : 0;
    }

    @Override
    public List<Identifier> getRelevantInputs() {
        return List.of(button);
    }

    @Override
    public InputType<?> type() {
        return InputType.BUTTON;
    }
}
