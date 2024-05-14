package dev.isxander.controlify.bindings.v2.inputmask;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record AxisBind(ResourceLocation axis) implements Bind {
    public static final String BIND_ID = "axis";

    public static final MapCodec<AxisBind> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf(BIND_ID).forGetter(AxisBind::axis)
    ).apply(instance, AxisBind::new));

    @Override
    public float state(ControllerStateView state) {
        return state.getAxisState(axis);
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of(axis);
    }

    @Override
    public BindType<?> type() {
        return BindType.AXIS;
    }
}
