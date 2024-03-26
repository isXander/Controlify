package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface IBind {
    float state(ControllerStateView state);

    void draw(GuiGraphics graphics, int x, int centerY, ControllerEntity controller);
    DrawSize drawSize(ControllerEntity controller);

    JsonObject toJson();

    static IBind fromJson(JsonObject json) {
        var type = json.get("type").getAsString();

        return switch (type) {
            case EmptyBind.BIND_ID -> new EmptyBind();

            case ButtonBind.BIND_ID -> {
                var button = new ResourceLocation(json.get("button").getAsString());
                yield new ButtonBind(button);
            }
            case AxisBind.BIND_ID -> {
                var axis = new ResourceLocation(json.get("axis").getAsString());
                yield new AxisBind(axis);
            }
            case HatBind.BIND_ID -> {
                var hat = new ResourceLocation(json.get("hat").getAsString());
                var targetState = json.get("targetState").getAsString();
                yield new HatBind(hat, HatState.valueOf(targetState));
            }
            default -> throw new IllegalArgumentException("Unknown bind type: " + type);
        };
    }
}
