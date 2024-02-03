package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.composable.ComposableControllerState;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;

public class EmptyBind implements IBind {
    public static final String BIND_ID = "empty";

    @Override
    public float state(ComposableControllerState state) {
        return 0;
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int centerY, Controller<?> controller) {

    }

    @Override
    public DrawSize drawSize() {
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
