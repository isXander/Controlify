package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EmptyBind implements IBind {
    public static final String BIND_ID = "empty";

    public static boolean equals(IBind bind) {
        return bind instanceof EmptyBind;
    }

    @Override
    public float state(ControllerStateView state) {
        return 0;
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of();
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        return object;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptyBind;
    }
}
