package dev.isxander.controlify.bindings.input;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.HatState;
import net.minecraft.resources.Identifier;

import java.util.List;

public record HatInput(Identifier hat, HatState targetState) implements Input {
    public static final String INPUT_ID = "hat";

    public static final MapCodec<HatInput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf(INPUT_ID).forGetter(HatInput::hat),
            HatState.CODEC.fieldOf("target_state").forGetter(HatInput::targetState)
    ).apply(instance, HatInput::new));

    @Override
    public float state(ControllerStateView state) {
        return state.getHatState(hat) == targetState ? 1 : 0;
    }

    @Override
    public List<Identifier> getRelevantInputs() {
        return List.of(hat);
    }

    @Override
    public InputType<?> type() {
        return InputType.HAT;
    }
}
