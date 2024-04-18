package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EmptyBind implements IBind {
    public static final String BIND_ID = "empty";

    @Override
    public float state(ControllerStateView state) {
        return 0;
    }

    @Override
    public List<ResourceLocation> getRelevantInputs() {
        return List.of();
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int centerY, ControllerEntity controller) {

    }

    @Override
    public DrawSize drawSize(ControllerEntity controller) {
        return new DrawSize(0, 0);
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
