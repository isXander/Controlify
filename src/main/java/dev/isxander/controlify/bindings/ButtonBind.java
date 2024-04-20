package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ButtonBind implements IBind {
    public static final String BIND_ID = "button";

    private final ResourceLocation button;

    public ButtonBind(ResourceLocation button) {
        this.button = button;
    }

    @Override
    public float state(ControllerStateView state) {
        return state.isButtonDown(button) ? 1 : 0;
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of(button);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("button", button.toString());

        return object;
    }
}
