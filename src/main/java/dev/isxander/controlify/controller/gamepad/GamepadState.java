package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.utils.ControllerUtils;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;

public final class GamepadState implements ControllerState {
    public static final GamepadState EMPTY = new GamepadState(AxesState.EMPTY, AxesState.EMPTY, ButtonState.EMPTY);
    private final AxesState gamepadAxes;
    private final AxesState rawGamepadAxes;
    private final ButtonState gamepadButtons;

    private final List<Float> unnamedAxes;
    private final List<Float> unnamedRawAxes;
    private final List<Boolean> unnamedButtons;

    public GamepadState(AxesState gamepadAxes, AxesState rawGamepadAxes, ButtonState gamepadButtons) {
        this.gamepadAxes = gamepadAxes;
        this.rawGamepadAxes = rawGamepadAxes;
        this.gamepadButtons = gamepadButtons;

        this.unnamedAxes = List.of(
                gamepadAxes.leftStickX(),
                gamepadAxes.leftStickY(),
                gamepadAxes.rightStickX(),
                gamepadAxes.rightStickY(),
                gamepadAxes.leftTrigger(),
                gamepadAxes.rightTrigger()
        );

        this.unnamedRawAxes = List.of(
                rawGamepadAxes.leftStickX(),
                rawGamepadAxes.leftStickY(),
                rawGamepadAxes.rightStickX(),
                rawGamepadAxes.rightStickY(),
                rawGamepadAxes.leftTrigger(),
                rawGamepadAxes.rightTrigger()
        );

        this.unnamedButtons = List.of(
                gamepadButtons.a(),
                gamepadButtons.b(),
                gamepadButtons.x(),
                gamepadButtons.y(),
                gamepadButtons.leftBumper(),
                gamepadButtons.rightBumper(),
                gamepadButtons.back(),
                gamepadButtons.start(),
                gamepadButtons.leftStick(),
                gamepadButtons.rightStick(),
                gamepadButtons.dpadUp(),
                gamepadButtons.dpadDown(),
                gamepadButtons.dpadLeft(),
                gamepadButtons.dpadRight()
        );
    }

    @Override
    public List<Float> axes() {
        return unnamedAxes;
    }

    @Override
    public List<Float> rawAxes() {
        return unnamedRawAxes;
    }

    @Override
    public List<Boolean> buttons() {
        return unnamedButtons;
    }

    @Override
    public boolean hasAnyInput() {
        return !this.gamepadAxes().equals(AxesState.EMPTY) || !this.gamepadButtons().equals(ButtonState.EMPTY);
    }

    public AxesState gamepadAxes() {
        return gamepadAxes;
    }

    public AxesState rawGamepadAxes() {
        return rawGamepadAxes;
    }

    public ButtonState gamepadButtons() {
        return gamepadButtons;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GamepadState) obj;
        return Objects.equals(this.gamepadAxes, that.gamepadAxes) &&
                Objects.equals(this.rawGamepadAxes, that.rawGamepadAxes) &&
                Objects.equals(this.gamepadButtons, that.gamepadButtons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gamepadAxes, rawGamepadAxes, gamepadButtons);
    }

    @Override
    public String toString() {
        return "GamepadState[" +
                "gamepadAxes=" + gamepadAxes + ", " +
                "rawGamepadAxes=" + rawGamepadAxes + ", " +
                "gamepadButtons=" + gamepadButtons + ']';
    }


    public record AxesState(
            float leftStickX, float leftStickY,
            float rightStickX, float rightStickY,
            float leftTrigger, float rightTrigger
    ) {
        public static AxesState EMPTY = new AxesState(0, 0, 0, 0, 0, 0);

        public AxesState leftJoystickDeadZone(float deadZoneX, float deadZoneY) {
            return new AxesState(
                    ControllerUtils.deadzone(leftStickX, deadZoneX),
                    ControllerUtils.deadzone(leftStickY, deadZoneY),
                    rightStickX, rightStickY, leftTrigger, rightTrigger
            );
        }

        public AxesState rightJoystickDeadZone(float deadZoneX, float deadZoneY) {
            return new AxesState(
                    leftStickX, leftStickY,
                    ControllerUtils.deadzone(rightStickX, deadZoneX),
                    ControllerUtils.deadzone(rightStickY, deadZoneY),
                    leftTrigger, rightTrigger
            );
        }

        public AxesState leftTriggerDeadZone(float deadZone) {
            return new AxesState(
                    leftStickX, leftStickY, rightStickX, rightStickY,
                    ControllerUtils.deadzone(leftTrigger, deadZone),
                    rightTrigger
            );
        }

        public AxesState rightTriggerDeadZone(float deadZone) {
            return new AxesState(
                    leftStickX, leftStickY, rightStickX, rightStickY,
                    leftTrigger,
                    ControllerUtils.deadzone(rightTrigger, deadZone)
            );
        }

        public static AxesState fromController(GamepadController controller) {
            if (controller == null)
                return EMPTY;

            var state = controller.getGamepadState();
            var axes = state.axes();

            float leftX = axes.get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
            float leftY = axes.get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
            float rightX = axes.get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);
            float rightY = axes.get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);
            float leftTrigger = (axes.get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) + 1f) / 2f;
            float rightTrigger = (axes.get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) + 1f) / 2f;

            return new AxesState(leftX, leftY, rightX, rightY, leftTrigger, rightTrigger);
        }
    }

    public record ButtonState(
            boolean a, boolean b, boolean x, boolean y,
            boolean leftBumper, boolean rightBumper,
            boolean back, boolean start, boolean guide,
            boolean dpadUp, boolean dpadDown, boolean dpadLeft, boolean dpadRight,
            boolean leftStick, boolean rightStick
    ) {
        public static ButtonState EMPTY = new ButtonState(
                false, false, false, false,
                false, false,
                false, false, false,
                false, false, false, false,
                false, false
        );

        public static ButtonState fromController(GamepadController controller) {
            if (controller == null)
                return EMPTY;

            var state = controller.getGamepadState();
            var buttons = state.buttons();

            boolean a = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_A) == GLFW.GLFW_PRESS;
            boolean b = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_B) == GLFW.GLFW_PRESS;
            boolean x = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_X) == GLFW.GLFW_PRESS;
            boolean y = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_Y) == GLFW.GLFW_PRESS;
            boolean leftBumper = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER) == GLFW.GLFW_PRESS;
            boolean rightBumper = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER) == GLFW.GLFW_PRESS;
            boolean back = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_BACK) == GLFW.GLFW_PRESS;
            boolean start = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_START) == GLFW.GLFW_PRESS;
            boolean guide = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE) == GLFW.GLFW_PRESS;
            boolean dpadUp = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP) == GLFW.GLFW_PRESS;
            boolean dpadDown = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == GLFW.GLFW_PRESS;
            boolean dpadLeft = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) == GLFW.GLFW_PRESS;
            boolean dpadRight = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) == GLFW.GLFW_PRESS;
            boolean leftStick = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB) == GLFW.GLFW_PRESS;
            boolean rightStick = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB) == GLFW.GLFW_PRESS;

            return new ButtonState(a, b, x, y, leftBumper, rightBumper, back, start, guide, dpadUp, dpadDown, dpadLeft, dpadRight, leftStick, rightStick);
        }
    }
}
