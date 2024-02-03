package dev.isxander.controlify.driver.gamepad;

import com.sun.jna.Memory;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.FloatByReference;
import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.controller.composable.ComposableControllerState;
import dev.isxander.controlify.controller.composable.TouchpadState;
import dev.isxander.controlify.controller.composable.gyro.GyroState;
import dev.isxander.controlify.controller.composable.impl.ComposableControllerStateImpl;
import dev.isxander.controlify.controller.composable.gamepad.GamepadInputs;
import dev.isxander.controlify.driver.*;
import dev.isxander.controlify.utils.CUtil;
import io.github.libsdl4j.api.gamepad.SDL_Gamepad;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;
import io.github.libsdl4j.api.properties.SDL_PropertiesID;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.ArrayList;
import java.util.List;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.events.SdlEventsConst.*;
import static io.github.libsdl4j.api.gamepad.SDL_GamepadAxis.*;
import static io.github.libsdl4j.api.gamepad.SDL_GamepadButton.*;
import static io.github.libsdl4j.api.gamepad.SdlGamepad.*;
import static io.github.libsdl4j.api.gamepad.SdlGamepadPropsConst.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;
import static io.github.libsdl4j.api.joystick.SDL_JoystickPowerLevel.*;
import static io.github.libsdl4j.api.properties.SdlProperties.*;
import static io.github.libsdl4j.api.sensor.SDL_SensorType.*;

public class SDL3GamepadDriver implements InputDriver, RumbleDriver, BatteryDriver, GUIDProvider, NameProviderDriver {
    private final SDL_Gamepad ptrGamepad;
    private ComposableControllerState state = ComposableControllerState.EMPTY;
    private final boolean isGyroSupported;
    private final boolean isRumbleSupported, isTriggerRumbleSupported;

    private final boolean isTouchpadSupported;
    private final int maxTouchpadFingers;

    private final String guid;
    private final String name;

    public SDL3GamepadDriver(SDL_JoystickID jid) {
        this.ptrGamepad =  SDL_OpenGamepad(jid);
        if (ptrGamepad == null)
            throw new IllegalStateException("Could not open gamepad: " + SDL_GetError());

        SDL_PropertiesID propertiesID = SDL_GetGamepadProperties(ptrGamepad);

        this.name = SDL_GetGamepadName(ptrGamepad);
        this.guid = SDL_GetGamepadInstanceGUID(jid).toString();
        this.isGyroSupported = SDL_GamepadHasSensor(ptrGamepad, SDL_SENSOR_GYRO);
        this.isRumbleSupported = SDL_GetBooleanProperty(propertiesID, SDL_PROP_GAMEPAD_CAP_RUMBLE_BOOLEAN, false);
        this.isTriggerRumbleSupported = SDL_GetBooleanProperty(propertiesID, SDL_PROP_GAMEPAD_CAP_TRIGGER_RUMBLE_BOOLEAN, false);
        this.isTouchpadSupported = SDL_GetNumGamepadTouchpads(ptrGamepad) > 0;
        this.maxTouchpadFingers = SDL_GetNumGamepadTouchpadFingers(ptrGamepad, 0);

        if (this.isGyroSupported()) {
            SDL_SetGamepadSensorEnabled(ptrGamepad, SDL_SENSOR_GYRO, true);
        }
    }

    @Override
    public void update() {
        ComposableControllerStateImpl state = new ComposableControllerStateImpl();
        // Axis values are in the range [-32768, 32767] (short)
        // https://wiki.libsdl.org/SDL3/SDL_GameControllerGetAxis
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFTX))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFTX))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_UP, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFTY))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_DOWN, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFTY))));

        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHTX))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHTX))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_UP, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHTY))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_DOWN, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHTY))));

        // Triggers are in the range [0, 32767] (thanks SDL!)
        state.setAxis(GamepadInputs.LEFT_TRIGGER_AXIS, mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFT_TRIGGER)));
        state.setAxis(GamepadInputs.RIGHT_TRIGGER_AXIS, mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHT_TRIGGER)));

        state.setButton(GamepadInputs.SOUTH_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_SOUTH) == SDL_PRESSED);
        state.setButton(GamepadInputs.EAST_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_EAST) == SDL_PRESSED);
        state.setButton(GamepadInputs.WEST_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_WEST) == SDL_PRESSED);
        state.setButton(GamepadInputs.NORTH_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_NORTH) == SDL_PRESSED);

        state.setButton(GamepadInputs.LEFT_SHOULDER_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_LEFT_SHOULDER) == SDL_PRESSED);
        state.setButton(GamepadInputs.RIGHT_SHOULDER_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER) == SDL_PRESSED);

        state.setButton(GamepadInputs.BACK_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_BACK) == SDL_PRESSED);
        state.setButton(GamepadInputs.START_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_START) == SDL_PRESSED);
        state.setButton(GamepadInputs.GUIDE_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_GUIDE) == SDL_PRESSED);

        state.setButton(GamepadInputs.DPAD_UP_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_DPAD_UP) == SDL_PRESSED);
        state.setButton(GamepadInputs.DPAD_DOWN_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_DPAD_DOWN) == SDL_PRESSED);
        state.setButton(GamepadInputs.DPAD_LEFT_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_DPAD_LEFT) == SDL_PRESSED);
        state.setButton(GamepadInputs.DPAD_RIGHT_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_DPAD_RIGHT) == SDL_PRESSED);

        state.setButton(GamepadInputs.LEFT_STICK_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_LEFT_STICK) == SDL_PRESSED);
        state.setButton(GamepadInputs.RIGHT_STICK_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_RIGHT_STICK) == SDL_PRESSED);

        // Additional inputs
        state.setButton(GamepadInputs.MISC_1_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_MISC1) == SDL_PRESSED);
        state.setButton(GamepadInputs.LEFT_PADDLE_1_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_LEFT_PADDLE1) == SDL_PRESSED);
        state.setButton(GamepadInputs.LEFT_PADDLE_2_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_LEFT_PADDLE2) == SDL_PRESSED);
        state.setButton(GamepadInputs.RIGHT_PADDLE_1_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1) == SDL_PRESSED);
        state.setButton(GamepadInputs.RIGHT_PADDLE_2_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2) == SDL_PRESSED);
        state.setButton(GamepadInputs.TOUCHPAD_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_TOUCHPAD) == SDL_PRESSED);

        if (isGyroSupported()) {
            float[] gyro = new float[3];

            try (Memory memory = new Memory(gyro.length * Float.BYTES)) {
                if (SDL_GetGamepadSensorData(ptrGamepad, SDL_SENSOR_GYRO, memory, 3) == 0) {
                    memory.read(0, gyro, 0, gyro.length);

                    state.setGyroState(new GyroState(gyro[0], gyro[1], gyro[2]));
                } else {
                    CUtil.LOGGER.error("Could not get gyro data: " + SDL_GetError());
                }
            }
        }

        if (isTouchpadSupported) {
            List<TouchpadState.Finger> fingers = new ArrayList<>();

            for (int finger = 0; finger < maxTouchpadFingers; finger++) {
                var fingerState = new ByteByReference();
                var x = new FloatByReference();
                var y = new FloatByReference();
                var pressure = new FloatByReference();

                if (SDL_GetGamepadTouchpadFinger(ptrGamepad, 0, finger, fingerState, x, y, pressure) != 0) {
                    CUtil.LOGGER.error("Failed to fetch touchpad finger: {}", SDL_GetError());
                } else if (fingerState.getValue() == 0x1) {
                    fingers.add(new TouchpadState.Finger(new Vector2f(x.getValue(), y.getValue()), pressure.getValue()));
                }
            }

            state.setTouchpadState(new TouchpadState(fingers, maxTouchpadFingers));
        }

        this.state = state;
    }

    @Override
    public int numButtons() {
        return 21;
    }

    @Override
    public int numAxes() {
        return 10;
    }

    @Override
    public int numHats() {
        return 0;
    }

    private float positiveAxis(float value) {
        return value < 0 ? 0 : value;
    }

    private float negativeAxis(float value) {
        return value > 0 ? 0 : -value;
    }

    @Override
    public ComposableControllerState getInputState() {
        return state;
    }

    @Override
    public boolean rumble(float strongMagnitude, float weakMagnitude) {
        if (!isRumbleSupported()) return false;

        // duration of 0 is infinite
        if (SDL_RumbleGamepad(ptrGamepad, (short)(strongMagnitude * 0xFFFF), (short)(weakMagnitude * 0xFFFF), 0) != 0) {
            CUtil.LOGGER.error("Could not rumble controller: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean rumbleTrigger(float left, float right) {
        if (!isTriggerRumbleSupported()) return false;

        // duration of 0 is infinite
        if (SDL_RumbleGamepadTriggers(ptrGamepad, (short)(left * 0xFFFF), (short)(right * 0xFFFF), 0) != 0) {
            CUtil.LOGGER.error("Could not rumble controller trigger: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BatteryLevel getBatteryLevel() {
        int powerLevel = SDL_GetGamepadPowerLevel(ptrGamepad);
        return switch (powerLevel) {
            case SDL_JOYSTICK_POWER_UNKNOWN -> BatteryLevel.UNKNOWN;
            case SDL_JOYSTICK_POWER_EMPTY -> BatteryLevel.EMPTY;
            case SDL_JOYSTICK_POWER_LOW -> BatteryLevel.LOW;
            case SDL_JOYSTICK_POWER_MEDIUM -> BatteryLevel.MEDIUM;
            case SDL_JOYSTICK_POWER_FULL -> BatteryLevel.FULL;
            case SDL_JOYSTICK_POWER_WIRED -> BatteryLevel.WIRED;
            case SDL_JOYSTICK_POWER_MAX -> BatteryLevel.MAX;
            default -> throw new IllegalStateException("Unexpected value: " + powerLevel);
        };
    }

    @Override
    public boolean isGyroSupported() {
        return isGyroSupported;
    }

    @Override
    public boolean isRumbleSupported() {
        return isRumbleSupported;
    }

    @Override
    public boolean isTriggerRumbleSupported() {
        return isTriggerRumbleSupported;
    }

    @Override
    public String getGUID() {
        return guid;
    }

    @Override
    public void close() {
        SDL_CloseGamepad(ptrGamepad);
    }

    @Override
    public String getRumbleDetails() {
        return "SDL3gp supported=" + isRumbleSupported() + " trigger=" + isTriggerRumbleSupported();
    }

    @Override
    public String getNameProviderDetails() {
        return "SDL3gp";
    }

    @Override
    public String getBatteryDriverDetails() {
        return "SDL3gp";
    }

    @Override
    public String getGUIDProviderDetails() {
        return "SDL3gp";
    }

    @Override
    public String getInputDriverDetails() {
        return "SDL3gp.gyro=" + isGyroSupported();
    }

    private static float mapShortToFloat(short value) {
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }
}
