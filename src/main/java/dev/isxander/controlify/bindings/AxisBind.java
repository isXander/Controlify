package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class AxisBind implements IBind {
    public static final String BIND_ID = "axis";

    private final ResourceLocation axis;

    public AxisBind(ResourceLocation axis) {
        this.axis = axis;
    }

    @Override
    public float state(ControllerStateView state) {
        return state.getAxisState(axis);
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of(axis);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("axis", axis.toString());

        return object;
    }
}
