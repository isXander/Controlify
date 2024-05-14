package dev.isxander.controlify.bindings.v2.inputmask;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ButtonBind(ResourceLocation button) implements Bind {
    public static final String BIND_ID = "button";

    public static final MapCodec<ButtonBind> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf(BIND_ID).forGetter(ButtonBind::button)
    ).apply(instance, ButtonBind::new));

    @Override
    public float state(ControllerStateView state) {
        return state.isButtonDown(button) ? 1 : 0;
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of(button);
    }

    @Override
    public BindType<?> type() {
        return BindType.BUTTON;
    }
}
