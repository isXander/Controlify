package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class JoystickAxisBind implements IBind<JoystickState> {
    public static final String BIND_ID = "joystick_axis";

    private final JoystickController joystick;
    private final int axisIndex;
    private final AxisDirection direction;

    public JoystickAxisBind(JoystickController joystick, int axisIndex, AxisDirection direction) {
        this.joystick = joystick;
        this.axisIndex = axisIndex;
        this.direction = direction;
    }

    @Override
    public float state(JoystickState state) {
        var rawState = state.axes().get(axisIndex);
        return switch (direction) {
            case POSITIVE -> Math.max(0, rawState);
            case NEGATIVE -> -Math.min(0, rawState);
        };
    }

    @Override
    public void draw(PoseStack matrices, int x, int centerY, Controller<JoystickState, ?> controller) {
        var font = Minecraft.getInstance().font;
        font.drawShadow(matrices, getTempButtonName(), x + 1.5f, centerY - font.lineHeight / 2f, 0xFFFFFF);
    }

    @Override
    public DrawSize drawSize() {
        var font = Minecraft.getInstance().font;
        return new DrawSize(font.width(getTempButtonName()) + 3, font.lineHeight);
    }

    private Component getTempButtonName() {
        var axis = joystick.mapping().axis(axisIndex);
        return Component.empty()
                .append(axis.name())
                .append(" ")
                .append(axis.getDirectionName(axisIndex, direction));
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("axis", axisIndex);
        object.addProperty("direction", direction.name());
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoystickAxisBind that = (JoystickAxisBind) o;
        return axisIndex == that.axisIndex && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(axisIndex, direction);
    }

    public static JoystickAxisBind fromJson(JsonObject object, JoystickController joystick) {
        var axisIndex = object.get("axis").getAsInt();
        var direction = AxisDirection.valueOf(object.get("direction").getAsString());
        return new JoystickAxisBind(joystick, axisIndex, direction);
    }

    public enum AxisDirection {
        POSITIVE,
        NEGATIVE
    }
}
