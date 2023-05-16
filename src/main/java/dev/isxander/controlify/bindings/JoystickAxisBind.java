package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class JoystickAxisBind implements IBind<JoystickState> {
    public static final String BIND_ID = "joystick_axis";

    private final JoystickController<?> joystick;
    private final int axisIndex;
    private final AxisDirection direction;

    public JoystickAxisBind(JoystickController<?> joystick, int axisIndex, AxisDirection direction) {
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
    public void draw(GuiGraphics graphics, int x, int centerY) {
        JoystickMapping mapping = joystick.mapping();

        String type = joystick.type().themeId();
        String axis = mapping.axes()[axisIndex].identifier();
        String direction = mapping.axes()[axisIndex].getDirectionIdentifier(axisIndex, this.direction);
        var texture = new ResourceLocation("controlify", "textures/gui/joystick/" + type + "/axis_" + axis + "_" + direction + ".png");

        graphics.blit(texture, x, centerY - 11, 0, 0, 22, 22, 22, 22);

        if (mapping instanceof UnmappedJoystickMapping) {
            var text = Integer.toString(axisIndex + 1);
            var font = Minecraft.getInstance().font;
            graphics.drawCenteredString(font, text, x + 11, centerY - font.lineHeight / 2, 0xFFFFFF);
        }
    }

    @Override
    public DrawSize drawSize() {
        int width = 22;
        if (joystick.mapping() instanceof UnmappedJoystickMapping)
            width = Math.max(width, Minecraft.getInstance().font.width(Integer.toString(axisIndex + 1)));

        return new DrawSize(width, 22);
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
    public Controller<JoystickState, ?> controller() {
        return joystick;
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

    public static JoystickAxisBind fromJson(JsonObject object, JoystickController<?> joystick) {
        var axisIndex = object.get("axis").getAsInt();
        var direction = AxisDirection.valueOf(object.get("direction").getAsString());
        return new JoystickAxisBind(joystick, axisIndex, direction);
    }

    public enum AxisDirection {
        POSITIVE,
        NEGATIVE
    }
}
