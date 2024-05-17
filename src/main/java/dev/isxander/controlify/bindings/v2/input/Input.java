package dev.isxander.controlify.bindings.v2.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface Input {
    MapCodec<Input> MAP_CODEC = InputType.createCodec(InputType.TYPES, InputType::codec, Input::type, "type");
    Codec<Input> CODEC = MAP_CODEC.codec();

    float state(ControllerStateView state);

    List<ResourceLocation> getRelevantInputs();

    InputType<?> type();
}
