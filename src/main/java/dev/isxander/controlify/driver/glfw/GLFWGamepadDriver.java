package dev.isxander.controlify.driver.glfw;

import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ControllerInfo;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWGamepadDriver implements Driver {
    private final int jid;
    private final String guid;

    private final ControllerEntity controller;

    public GLFWGamepadDriver(int jid, ControllerType type, String uid, UniqueControllerID ucid, Optional<HIDDevice> hid) {
        this.jid = jid;
        this.guid = glfwGetJoystickGUID(jid);

        this.getGamepadState(); // test input ability so the create catches it

        ControllerInfo info = new ControllerInfo(uid, ucid, this.guid, glfwGetGamepadName(jid), type, hid);
        this.controller = new ControllerEntity(info);

        this.controller.setComponent(new InputComponent(this.controller, 15, 10, 0, true, GamepadInputs.DEADZONE_GROUPS, type.mappingId()), InputComponent.ID);

        this.controller.finalise();
    }

    @Override
    public void update(boolean outOfFocus) {
        this.updateInput();
    }

    @Override
    public void close() {

    }

    @Override
    public ControllerEntity getController() {
        return this.controller;
    }

    private void updateInput() {
        GLFWGamepadState glfwState = this.getGamepadState();
        ControllerStateImpl state = new ControllerStateImpl();

        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_DOWN, positiveAxis(glfwState.axes(org.lwjgl.glfw.GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_RIGHT, positiveAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_UP, negativeAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_LEFT, negativeAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)));

        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_UP, negativeAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y)));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_LEFT, negativeAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X)));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_DOWN, positiveAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y)));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, positiveAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X)));

        state.setAxis(GamepadInputs.LEFT_TRIGGER_AXIS, (1f + glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER)) / 2f);
        state.setAxis(GamepadInputs.RIGHT_TRIGGER_AXIS, (1f + glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER)) / 2f);

        state.setButton(GamepadInputs.SOUTH_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_A) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.EAST_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_B) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.WEST_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_X) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.NORTH_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_Y) == GLFW.GLFW_PRESS);

        state.setButton(GamepadInputs.LEFT_SHOULDER_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.RIGHT_SHOULDER_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER) == GLFW.GLFW_PRESS);

        state.setButton(GamepadInputs.LEFT_STICK_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.RIGHT_STICK_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB) == GLFW.GLFW_PRESS);

        state.setButton(GamepadInputs.BACK_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_BACK) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.START_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_START) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.GUIDE_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE) == GLFW.GLFW_PRESS);

        state.setButton(GamepadInputs.DPAD_UP_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.DPAD_DOWN_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.DPAD_LEFT_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.DPAD_RIGHT_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) == GLFW.GLFW_PRESS);

        this.controller.<InputComponent>getComponent(InputComponent.ID).orElseThrow().pushState(state);
    }

    private GLFWGamepadState getGamepadState() {
        GLFWGamepadState state = GLFWGamepadState.create();
        glfwGetGamepadState(jid, state);
        return state;
    }

    private float positiveAxis(float value) {
        return value < 0 ? 0 : value;
    }

    private float negativeAxis(float value) {
        return value > 0 ? 0 : -value;
    }

    private static float mapShortToFloat(short value) {
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }
}
