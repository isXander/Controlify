package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.utils.ControllerUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Objects;

public final class GamepadState implements ControllerState {
    public static final GamepadState EMPTY = new GamepadState(AxesState.EMPTY, AxesState.EMPTY, ButtonState.EMPTY, new GyroState(), new GyroState());
    private final AxesState gamepadAxes;
    private final AxesState rawGamepadAxes;
    private final ButtonState gamepadButtons;

    private final GyroState absoluteGyroPos;
    private final @Nullable GyroState gyroDelta;

    private final List<Float> unnamedAxes;
    private final List<Float> unnamedRawAxes;
    private final List<Boolean> unnamedButtons;

    public GamepadState(
            AxesState gamepadAxes,
            AxesState rawGamepadAxes,
            ButtonState gamepadButtons,
            @Nullable GyroState gyroDelta,
            GyroState absoluteGyroPos
    ) {
        this.gamepadAxes = gamepadAxes;
        this.rawGamepadAxes = rawGamepadAxes;
        this.gamepadButtons = gamepadButtons;
        this.gyroDelta = gyroDelta;
        this.absoluteGyroPos = absoluteGyroPos;

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

    public GyroState gyroDelta() {
        if (gyroDelta == null) return new GyroState();
        return gyroDelta;
    }

    public GyroState absoluteGyroPos() {
        return absoluteGyroPos;
    }

    public boolean supportsGyro() {
        return gyroDelta != null;
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

        public AxesState leftJoystickDeadZone(float deadZone) {
            return new AxesState(
                    ControllerUtils.deadzone(leftStickX, deadZone),
                    ControllerUtils.deadzone(leftStickY, deadZone),
                    rightStickX, rightStickY, leftTrigger, rightTrigger
            );
        }

        public AxesState rightJoystickDeadZone(float deadZone) {
            return new AxesState(
                    leftStickX, leftStickY,
                    ControllerUtils.deadzone(rightStickX, deadZone),
                    ControllerUtils.deadzone(rightStickY, deadZone),
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

        public AxesState neutraliseLeft() {
            return new AxesState(0, 0, rightStickX, rightStickY, leftTrigger, rightTrigger);
        }

        public AxesState neutraliseRight() {
            return new AxesState(leftStickX, leftStickY, 0, 0, leftTrigger, rightTrigger);
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
    }

    public interface GyroStateC extends Vector3fc {
        GyroStateC ZERO = new GyroState(0, 0, 0);

        float pitch();

        float yaw();

        float roll();
    }

    public static class GyroState extends Vector3f implements GyroStateC {
        public GyroState(float pitch, float yaw, float roll) {
            super(pitch, yaw, roll);
        }

        public GyroState(GyroStateC vec) {
            super(vec);
        }

        public GyroState() {
        }

        @Override
        public float pitch() {
            return x;
        }

        @Override
        public float yaw() {
            return y;
        }

        @Override
        public float roll() {
            return z;
        }

        @Override
        public GyroState mul(Vector3fc v) {
            super.mul(v);
            return this;
        }

        @Override
        public GyroState mul(float scalar) {
            super.mul(scalar);
            return this;
        }

        @Override
        public GyroState div(Vector3fc v) {
            super.div(v);
            return this;
        }

        @Override
        public GyroState div(float scalar) {
            super.div(scalar);
            return this;
        }

        @Override
        public GyroState sub(Vector3fc v) {
            super.sub(v);
            return this;
        }

        @Override
        public GyroState sub(float x, float y, float z) {
            super.sub(x, y, z);
            return this;
        }
    }
}
