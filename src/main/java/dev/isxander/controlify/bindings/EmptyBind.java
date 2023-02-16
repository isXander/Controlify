package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.gui.DrawSize;

public class EmptyBind<T extends ControllerState> implements IBind<T> {
    public static final String BIND_ID = "empty";

    @Override
    public float state(T state) {
        return 0;
    }

    @Override
    public void draw(PoseStack matrices, int x, int centerY, Controller<T, ?> controller) {

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
