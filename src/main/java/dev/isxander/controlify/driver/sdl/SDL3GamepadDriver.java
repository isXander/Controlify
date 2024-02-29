package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Memory;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.FloatByReference;
import dev.isxander.controlify.controller.battery.BatteryLevel;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadState;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.controllermanager.SDLControllerManager;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.rumble.TriggerRumbleState;
import dev.isxander.controlify.utils.CUtil;
import io.github.libsdl4j.api.gamepad.SDL_Gamepad;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;
import io.github.libsdl4j.api.properties.SDL_PropertiesID;
import io.github.libsdl4j.api.sensor.SDL_SensorType;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.events.SdlEventsConst.*;
import static io.github.libsdl4j.api.gamepad.SDL_GamepadAxis.*;
import static io.github.libsdl4j.api.gamepad.SDL_GamepadButton.*;
import static io.github.libsdl4j.api.gamepad.SdlGamepad.*;
import static io.github.libsdl4j.api.gamepad.SdlGamepadPropsConst.*;
import static io.github.libsdl4j.api.joystick.SDL_JoystickPowerLevel.*;
import static io.github.libsdl4j.api.properties.SdlProperties.*;
import static io.github.libsdl4j.api.sensor.SDL_SensorType.*;

public class SDL3GamepadDriver implements Driver {
    private final SDL_Gamepad ptrGamepad;
    private final ControllerEntity controller;

    private final boolean isGryoSupported;
    private final boolean isRumbleSupported, isTriggerRumbleSupported;

    private final int numTouchpads;
    private final int maxTouchpadFingers;

    private final String guid;
    private final String name;

    private final UniqueControllerID ucid;

    public SDL3GamepadDriver(SDL_JoystickID jid, ControllerType type, String uid, UniqueControllerID ucid, Optional<HIDIdentifier> hid) {
        this.ptrGamepad = SDL_OpenGamepad(jid);
        if (this.ptrGamepad == null) {
            throw new IllegalStateException("Could not open gamepad: " + SDL_GetError());
        }
        this.ucid = new SDLControllerManager.SDLUniqueControllerID(SDL_GetGamepadInstanceID(ptrGamepad));

        SDL_PropertiesID properties = SDL_GetGamepadProperties(ptrGamepad);

        this.name = SDL_GetGamepadName(ptrGamepad);
        this.guid = SDL_GetGamepadInstanceGUID(jid).toString();
        this.isGryoSupported = SDL_GamepadHasSensor(ptrGamepad, SDL_SensorType.SDL_SENSOR_GYRO);
        this.isRumbleSupported = SDL_GetBooleanProperty(properties, SDL_PROP_GAMEPAD_CAP_RUMBLE_BOOLEAN, false);
        this.isTriggerRumbleSupported = SDL_GetBooleanProperty(properties, SDL_PROP_GAMEPAD_CAP_TRIGGER_RUMBLE_BOOLEAN, false);
        this.numTouchpads = SDL_GetNumGamepadTouchpads(ptrGamepad);
        this.maxTouchpadFingers = IntStream.range(0, numTouchpads).map(i -> SDL_GetNumGamepadTouchpadFingers(ptrGamepad, i)).sum();

        ControllerInfo info = new ControllerInfo(uid, ucid, this.guid, type, hid);
        this.controller = new ControllerEntity(info);

        this.controller.setComponent(new InputComponent(21, 10, 0, true, GamepadInputs.DEADZONE_GROUPS), InputComponent.ID);
        this.controller.setComponent(new BatteryLevelComponent(), BatteryLevelComponent.ID);
        if (this.isGryoSupported) {
            SDL_SetGamepadSensorEnabled(ptrGamepad, SDL_SensorType.SDL_SENSOR_GYRO, true);
            this.controller.setComponent(new GyroComponent(), GyroComponent.ID);
        }
        if (this.isRumbleSupported) {
            this.controller.setComponent(new RumbleComponent(), RumbleComponent.ID);
        }
        if (this.isTriggerRumbleSupported) {
            this.controller.setComponent(new TriggerRumbleComponent(), TriggerRumbleComponent.ID);
        }
        if (this.numTouchpads > 0) {
            this.controller.setComponent(new TouchpadComponent(this.maxTouchpadFingers), TouchpadComponent.ID);
        }

        this.controller.finalise();
    }

    public UniqueControllerID getUcid() {
        return this.ucid;
    }

    @Override
    public ControllerEntity getController() {
        return this.controller;
    }

    @Override
    public void update(boolean outOfFocus) {
        this.updateInput();
        this.updateRumble();
        this.updateGyro();
        this.updateTouchpad();
        this.updateBatteryLevel();
    }

    @Override
    public void close() {
        SDL_CloseGamepad(ptrGamepad);
    }

    private void updateInput() {
        ControllerStateImpl state = new ControllerStateImpl();
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

        this.controller.<InputComponent>getComponent(InputComponent.ID).orElseThrow().pushState(state);
    }

    private void updateRumble() {
        if (isRumbleSupported) {
            Optional<RumbleState> stateOpt = this.controller
                    .rumble()
                    .orElseThrow()
                    .consumeRumble();

            stateOpt.ifPresent(state -> {
                if (SDL_RumbleGamepad(ptrGamepad, (short)(state.strong() * 0xFFFF), (short)(state.weak() * 0xFFFF), 0) != 0) {
                    CUtil.LOGGER.error("Could not rumble gamepad: " + SDL_GetError());
                }
            });
        }

        if (isTriggerRumbleSupported) {
            Optional<TriggerRumbleState> stateOpt = this.controller
                    .triggerRumble()
                    .orElseThrow()
                    .consumeTriggerRumble();

            stateOpt.ifPresent(state -> {
                if (SDL_RumbleGamepadTriggers(ptrGamepad, (short)(state.left() * 0xFFFF), (short)(state.right() * 0xFFFF), 0) != 0) {
                    CUtil.LOGGER.error("Could not rumble triggers gamepad: " + SDL_GetError());
                }
            });
        }
    }

    private void updateGyro() {
        if (!isGryoSupported) return;

        float[] gyro = new float[3];

        try (Memory memory = new Memory(gyro.length * Float.BYTES)) {
            if (SDL_GetGamepadSensorData(ptrGamepad, SDL_SENSOR_GYRO, memory, 3) == 0) {
                memory.read(0, gyro, 0, gyro.length);

                this.controller.gyro().orElseThrow().setState(
                        new GyroState(gyro[0], gyro[1], gyro[2])
                );
            } else {
                CUtil.LOGGER.error("Could not get gyro data: " + SDL_GetError());
            }
        }
    }

    private void updateTouchpad() {
        if (numTouchpads == 0) return;

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

        this.controller.touchpad().orElseThrow().pushFingers(fingers);
    }

    private void updateBatteryLevel() {
        BatteryLevel level = switch (SDL_GetGamepadPowerLevel(ptrGamepad)) {
            case SDL_JOYSTICK_POWER_UNKNOWN -> BatteryLevel.UNKNOWN;
            case SDL_JOYSTICK_POWER_EMPTY -> BatteryLevel.EMPTY;
            case SDL_JOYSTICK_POWER_LOW -> BatteryLevel.LOW;
            case SDL_JOYSTICK_POWER_MEDIUM -> BatteryLevel.MEDIUM;
            case SDL_JOYSTICK_POWER_FULL -> BatteryLevel.FULL;
            case SDL_JOYSTICK_POWER_WIRED -> BatteryLevel.WIRED;
            case SDL_JOYSTICK_POWER_MAX -> BatteryLevel.MAX;
            default -> throw new IllegalStateException("Unexpected value");
        };

        this.controller.batteryLevel().orElseThrow().setBatteryLevel(level);
    }

    private static float positiveAxis(float value) {
        return value < 0 ? 0 : value;
    }

    private static float negativeAxis(float value) {
        return value > 0 ? 0 : -value;
    }

    private static float mapShortToFloat(short value) {
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }
}
