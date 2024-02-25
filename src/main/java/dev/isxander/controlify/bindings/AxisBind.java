package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.Inputs;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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
    public void draw(GuiGraphics graphics, int x, int centerY, ControllerEntity controller) {
        graphics.blitSprite(Inputs.getThemedSprite(axis, controller.info().type().namespace()), x, centerY  - 11, 22, 22);
    }

    @Override
    public DrawSize drawSize() {
        return new DrawSize(22, 22);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("axis", axis.toString());

        return object;
    }
}
