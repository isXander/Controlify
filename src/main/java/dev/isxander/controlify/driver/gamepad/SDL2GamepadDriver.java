package dev.isxander.controlify.driver.gamepad;

import com.sun.jna.Memory;
import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.*;
import dev.isxander.controlify.utils.CUtil;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import net.minecraft.util.Mth;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.gamecontroller.SDL_GameControllerAxis.*;
import static io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton.*;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*;
import static io.github.libsdl4j.api.joystick.SDL_JoystickPowerLevel.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;
import static io.github.libsdl4j.api.sensor.SDL_SensorType.*;

public class SDL2GamepadDriver implements BasicGamepadInputDriver, GyroDriver, RumbleDriver, BatteryDriver, GUIDProvider {
    private final SDL_GameController ptrGamepad;
    private BasicGamepadState state = BasicGamepadState.EMPTY;
    private GamepadState.GyroState gyroDelta = new GamepadState.GyroState(0, 0, 0);
    private final boolean isGyroSupported, isRumbleSupported, isTriggerRumbleSupported;
    private final String guid;

    public SDL2GamepadDriver(int jid) {
        this.ptrGamepad =  SDL_GameControllerOpen(jid);
        if (ptrGamepad == null)
            throw new IllegalStateException("Could not open gamepad: " + SDL_GetError());

        this.guid = SDL_JoystickGetGUID(SDL_GameControllerGetJoystick(ptrGamepad)).toString();
        this.isGyroSupported = SDL_GameControllerHasSensor(ptrGamepad, SDL_SENSOR_GYRO);
        this.isRumbleSupported = SDL_GameControllerHasRumble(ptrGamepad);
        this.isTriggerRumbleSupported = SDL_GameControllerHasRumbleTriggers(ptrGamepad);

        if (this.isGyroSupported()) {
            SDL_GameControllerSetSensorEnabled(ptrGamepad, SDL_SENSOR_GYRO, true);
        }
    }

    @Override
    public void update() {
        if (isGyroSupported()) {
            float[] gyro = new float[3];

            try (Memory memory = new Memory(gyro.length * Float.BYTES)) {
                if (SDL_GameControllerGetSensorData(ptrGamepad, SDL_SENSOR_GYRO, memory, 3) == 0) {
                    memory.read(0, gyro, 0, gyro.length);

                    gyroDelta = new GamepadState.GyroState(gyro[0], gyro[1], gyro[2]);
                    if (DebugProperties.PRINT_GYRO) CUtil.LOGGER.info("Gyro delta: " + gyroDelta);
                } else {
                    CUtil.LOGGER.error("Could not get gyro data: " + SDL_GetError());
                }
            }

        }
        SDL_GameControllerUpdate();

        // Axis values are in the range [-32768, 32767] (short)
        // Triggers are in the range [0, 32767] (thanks SDL!)
        // https://wiki.libsdl.org/SDL2/SDL_GameControllerGetAxis
        GamepadState.AxesState axes = new GamepadState.AxesState(
                mapShortToFloat(SDL_GameControllerGetAxis(ptrGamepad, SDL_CONTROLLER_AXIS_LEFTX)),
                mapShortToFloat(SDL_GameControllerGetAxis(ptrGamepad, SDL_CONTROLLER_AXIS_LEFTY)),
                mapShortToFloat(SDL_GameControllerGetAxis(ptrGamepad, SDL_CONTROLLER_AXIS_RIGHTX)),
                mapShortToFloat(SDL_GameControllerGetAxis(ptrGamepad, SDL_CONTROLLER_AXIS_RIGHTY)),
                mapShortToFloat(SDL_GameControllerGetAxis(ptrGamepad, SDL_CONTROLLER_AXIS_TRIGGERLEFT)),
                mapShortToFloat(SDL_GameControllerGetAxis(ptrGamepad, SDL_CONTROLLER_AXIS_TRIGGERRIGHT))
        );
        // Button values return 1 if pressed, 0 if not
        // https://wiki.libsdl.org/SDL2/SDL_GameControllerGetButton
        GamepadState.ButtonState buttons = new GamepadState.ButtonState(
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_A) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_B) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_X) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_Y) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_LEFTSHOULDER) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_RIGHTSHOULDER) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_BACK) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_START) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_GUIDE) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_DPAD_UP) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_DPAD_DOWN) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_DPAD_LEFT) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_DPAD_RIGHT) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_LEFTSTICK) == 1,
                SDL_GameControllerGetButton(ptrGamepad, SDL_CONTROLLER_BUTTON_RIGHTSTICK) == 1
        );
        this.state = new BasicGamepadState(axes, buttons);
    }

    @Override
    public BasicGamepadState getBasicGamepadState() {
        return state;
    }

    @Override
    public boolean rumble(float strongMagnitude, float weakMagnitude) {
        if (!isRumbleSupported()) return false;

        // duration of 0 is infinite
        if (SDL_GameControllerRumble(ptrGamepad, (short)(strongMagnitude * 0xFFFF), (short)(weakMagnitude * 0xFFFF), 0) != 0) {
            CUtil.LOGGER.error("Could not rumble controller: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean rumbleTrigger(float left, float right) {
        if (!isTriggerRumbleSupported()) return false;

        // duration of 0 is infinite
        if (SDL_GameControllerRumbleTriggers(ptrGamepad, (short)(left * 0xFFFF), (short)(right * 0xFFFF), 0) != 0) {
            CUtil.LOGGER.error("Could not rumble controller trigger: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public GamepadState.GyroStateC getGyroState() {
        return gyroDelta;
    }

    @Override
    public BatteryLevel getBatteryLevel() {
        int powerLevel = SDL_JoystickCurrentPowerLevel(SDL_GameControllerGetJoystick(ptrGamepad));
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
        SDL_GameControllerClose(ptrGamepad);
    }

    @Override
    public String getGyroDetails() {
        return "SDL2gp supported=" + isGyroSupported();
    }

    @Override
    public String getRumbleDetails() {
        return "SDL2gp supported=" + isRumbleSupported() + " trigger=" + isTriggerRumbleSupported();
    }

    @Override
    public String getBatteryDriverDetails() {
        return "SDL2gp";
    }

    @Override
    public String getGUIDProviderDetails() {
        return "SDL2gp";
    }

    @Override
    public String getBasicGamepadDetails() {
        return "SDL2gp";
    }

    private static float mapShortToFloat(short value) {
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }
}
