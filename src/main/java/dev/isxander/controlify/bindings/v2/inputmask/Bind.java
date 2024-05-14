package dev.isxander.controlify.bindings.v2.inputmask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface Bind {
    MapCodec<Bind> MAP_CODEC = BindType.createCodec(BindType.TYPES, BindType::codec, Bind::type, "type");
    Codec<Bind> CODEC = MAP_CODEC.codec();

    float state(ControllerStateView state);

    List<ResourceLocation> getRelevantInputs();

    BindType<?> type();
}
