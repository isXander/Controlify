package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

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
    public void draw(PoseStack matrices, int x, int centerY) {
        String type = joystick.type().identifier();
        String button = joystick.mapping().button(hatIndex).identifier();
        String direction = "centered";
        if (hatState.isUp())
            direction = "up";
        else if (hatState.isDown())
            direction = "down";
        else if (hatState.isLeft())
            direction = "left";
        else if (hatState.isRight())
            direction = "right";

        var texture = new ResourceLocation("controlify", "textures/gui/joystick/" + type + "/hat" + button + "_" + direction + ".png");

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GuiComponent.blit(matrices, x, centerY - 11, 0, 0, 22, 22, 22, 22);
    }

    @Override
    public DrawSize drawSize() {
        return new DrawSize(22, 22);
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
    public Controller<JoystickState, ?> controller() {
        return joystick;
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
