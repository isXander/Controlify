package dev.isxander.controlify.bindings.v2.inputmask;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record EmptyBind() implements Bind {
    public static final EmptyBind INSTANCE = new EmptyBind();

    public static final String BIND_ID = "empty";
    public static final MapCodec<EmptyBind> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public float state(ControllerStateView state) {
        return 0;
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of();
    }

    @Override
    public BindType<?> type() {
        return BindType.EMPTY;
    }

    public static boolean equals(Bind bind) {
        return bind instanceof EmptyBind;
    }
}
