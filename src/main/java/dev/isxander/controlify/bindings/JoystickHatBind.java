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

public class JoystickHatBind implements IBind<JoystickState> {
    public static final String BIND_ID = "joystick_hat";

    private final JoystickController joystick;
    private final int hatIndex;
    private final JoystickState.HatState hatState;

    public JoystickHatBind(JoystickController joystick, int hatIndex, JoystickState.HatState hatState) {
        this.joystick = joystick;
        this.hatIndex = hatIndex;
        this.hatState = hatState;
    }

    @Override
    public float state(JoystickState state) {
        return state.hats().get(hatIndex) == hatState ? 1 : 0;
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
        return Component.empty()
                .append(joystick.mapping().hat(hatIndex).name())
                .append(" ")
                .append(hatState.getDisplayName());
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("hat", hatIndex);
        object.addProperty("state", hatState.name());
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoystickHatBind that = (JoystickHatBind) o;
        return hatIndex == that.hatIndex && hatState == that.hatState && joystick.uid().equals(that.joystick.uid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(hatIndex, hatState, joystick.uid());
    }

    public static JoystickHatBind fromJson(JsonObject object, JoystickController joystick) {
        var hatIndex = object.get("hat").getAsInt();
        var hatState = JoystickState.HatState.valueOf(object.get("state").getAsString());
        return new JoystickHatBind(joystick, hatIndex, hatState);
    }
}
