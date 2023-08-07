package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.utils.Log;
import net.minecraft.util.Mth;
import org.libsdl.SDL;

public class SDL2GamepadDriver implements BasicGamepadInputDriver, GyroDriver, RumbleDriver, BatteryDriver, GUIDProvider {
    private final long ptrGamepad;
    private BasicGamepadState state = BasicGamepadState.EMPTY;
    private GamepadState.GyroState gyroDelta = new GamepadState.GyroState(0, 0, 0);
    private final boolean isGyroSupported, isRumbleSupported;
    private final String guid;

    public SDL2GamepadDriver(int jid) {
        this.ptrGamepad =  SDL.SDL_GameControllerOpen(jid);
        this.guid = SDL.SDL_JoystickGUIDString(SDL.SDL_GameControllerGetJoystick(ptrGamepad));
        this.isGyroSupported = SDL.SDL_GameControllerHasSensor(ptrGamepad, SDL.SDL_SENSOR_GYRO);
        this.isRumbleSupported = SDL.SDL_GameControllerHasRumble(ptrGamepad);

        if (this.isGyroSupported()) {
            SDL.SDL_GameControllerSetSensorEnabled(ptrGamepad, SDL.SDL_SENSOR_GYRO, true);
        }
    }

    @Override
    public void update() {
        if (isGyroSupported()) {
            float[] gyro = new float[3];
            if (SDL.SDL_GameControllerGetSensorData(ptrGamepad, SDL.SDL_SENSOR_GYRO, gyro, 3) == 0) {
                gyroDelta = new GamepadState.GyroState(gyro[0], gyro[1], gyro[2]);
                if (DebugProperties.PRINT_GYRO) Log.LOGGER.info("Gyro delta: " + gyroDelta);
            } else {
                Log.LOGGER.error("Could not get gyro data: " + SDL.SDL_GetError());
            }

        }
        SDL.SDL_GameControllerUpdate();

        // Axis values are in the range [-32768, 32767] (short)
        // Triggers are in the range [0, 32767] (thanks SDL!)
        // https://wiki.libsdl.org/SDL2/SDL_GameControllerGetAxis
        GamepadState.AxesState axes = new GamepadState.AxesState(
                Mth.inverseLerp(SDL.SDL_GameControllerGetAxis(ptrGamepad, SDL.SDL_CONTROLLER_AXIS_LEFTX), Short.MIN_VALUE, Short.MAX_VALUE) * 2f - 1f,
                Mth.inverseLerp(SDL.SDL_GameControllerGetAxis(ptrGamepad, SDL.SDL_CONTROLLER_AXIS_LEFTY), Short.MIN_VALUE, Short.MAX_VALUE) * 2f - 1f,
                Mth.inverseLerp(SDL.SDL_GameControllerGetAxis(ptrGamepad, SDL.SDL_CONTROLLER_AXIS_RIGHTX), Short.MIN_VALUE, Short.MAX_VALUE) * 2f - 1f,
                Mth.inverseLerp(SDL.SDL_GameControllerGetAxis(ptrGamepad, SDL.SDL_CONTROLLER_AXIS_RIGHTY), Short.MIN_VALUE, Short.MAX_VALUE) * 2f - 1f,
                Mth.inverseLerp(SDL.SDL_GameControllerGetAxis(ptrGamepad, SDL.SDL_CONTROLLER_AXIS_TRIGGERLEFT), 0, Short.MAX_VALUE),
                Mth.inverseLerp(SDL.SDL_GameControllerGetAxis(ptrGamepad, SDL.SDL_CONTROLLER_AXIS_TRIGGERRIGHT), 0, Short.MAX_VALUE)
        );
        // Button values return 1 if pressed, 0 if not
        // https://wiki.libsdl.org/SDL2/SDL_GameControllerGetButton
        GamepadState.ButtonState buttons = new GamepadState.ButtonState(
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_A) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_B) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_X) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_Y) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_LEFTSHOULDER) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_RIGHTSHOULDER) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_BACK) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_START) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_GUIDE) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_DPAD_UP) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_DPAD_DOWN) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_DPAD_LEFT) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_DPAD_RIGHT) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_LEFTSTICK) == 1,
                SDL.SDL_GameControllerGetButton(ptrGamepad, SDL.SDL_CONTROLLER_BUTTON_RIGHTSTICK) == 1
        );
        this.state = new BasicGamepadState(axes, buttons);
    }

    @Override
    public BasicGamepadState getBasicGamepadState() {
        return state;
    }

    @Override
    public boolean rumble(float strongMagnitude, float weakMagnitude) {
        // duration of 0 is infinite
        if (!SDL.SDL_GameControllerRumble(ptrGamepad, (int)(strongMagnitude * 65535.0F), (int)(weakMagnitude * 65535.0F), 0)) {
            Log.LOGGER.error("Could not rumble controller: " + SDL.SDL_GetError());
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
        return switch (SDL.SDL_JoystickCurrentPowerLevel(ptrGamepad)) {
            case SDL.SDL_JOYSTICK_POWER_UNKNOWN -> BatteryLevel.UNKNOWN;
            case SDL.SDL_JOYSTICK_POWER_EMPTY -> BatteryLevel.EMPTY;
            case SDL.SDL_JOYSTICK_POWER_LOW -> BatteryLevel.LOW;
            case SDL.SDL_JOYSTICK_POWER_MEDIUM -> BatteryLevel.MEDIUM;
            case SDL.SDL_JOYSTICK_POWER_FULL -> BatteryLevel.FULL;
            case SDL.SDL_JOYSTICK_POWER_WIRED -> BatteryLevel.WIRED;
            case SDL.SDL_JOYSTICK_POWER_MAX -> BatteryLevel.MAX;
            default -> throw new IllegalStateException("Unexpected value: " + SDL.SDL_JoystickCurrentPowerLevel(ptrGamepad));
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
    public String getGUID() {
        return guid;
    }

    @Override
    public void close() {
        SDL.SDL_GameControllerClose(ptrGamepad);
    }

    @Override
    public String getGyroDetails() {
        return "SDL2gp supported=" + isGyroSupported();
    }

    @Override
    public String getRumbleDetails() {
        return "SDL2gp supported=" + isRumbleSupported();
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
}
