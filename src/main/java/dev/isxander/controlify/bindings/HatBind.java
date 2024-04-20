package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.Inputs;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class HatBind implements IBind {
    public static final String BIND_ID = "hat";

    private final ResourceLocation hat;
    private final HatState targetState;

    public HatBind(ResourceLocation hat, HatState targetState) {
        this.hat = hat;
        this.targetState = targetState;
    }

    @Override
    public float state(ControllerStateView state) {
        return state.getHatState(hat) == targetState ? 1 : 0;
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of(hat);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("hat", hat.toString());

        return object;
    }
}
