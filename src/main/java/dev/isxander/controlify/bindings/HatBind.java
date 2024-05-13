package dev.isxander.controlify.bindings;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.HatState;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record HatBind(ResourceLocation hat, HatState targetState) implements Bind {
    public static final String BIND_ID = "hat";

    public static final MapCodec<HatBind> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf(BIND_ID).forGetter(HatBind::hat),
            HatState.CODEC.fieldOf("target_state").forGetter(HatBind::targetState)
    ).apply(instance, HatBind::new));

    @Override
    public float state(ControllerStateView state) {
        return state.getHatState(hat) == targetState ? 1 : 0;
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of(hat);
    }

    @Override
    public BindType<?> type() {
        return BindType.HAT;
    }
}
