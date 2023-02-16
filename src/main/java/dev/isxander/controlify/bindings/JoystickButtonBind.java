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

public class JoystickButtonBind implements IBind<JoystickState> {
    public static final String BIND_ID = "joystick_button";

    private final JoystickController joystick;
    private final int buttonIndex;

    public JoystickButtonBind(JoystickController joystick, int buttonIndex) {
        this.joystick = joystick;
        this.buttonIndex = buttonIndex;
    }

    @Override
    public float state(JoystickState state) {
        return state.buttons().get(buttonIndex) ? 1 : 0;
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
        return joystick.mapping().button(buttonIndex).name();
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("button", buttonIndex);
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoystickButtonBind that = (JoystickButtonBind) o;
        return buttonIndex == that.buttonIndex && joystick.uid().equals(that.joystick.uid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(buttonIndex, joystick.uid());
    }

    public static JoystickButtonBind fromJson(JsonObject object, JoystickController joystick) {
        var buttonIndex = object.get("button").getAsInt();
        return new JoystickButtonBind(joystick, buttonIndex);
    }
}
