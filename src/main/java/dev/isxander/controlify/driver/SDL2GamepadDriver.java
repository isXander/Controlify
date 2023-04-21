package dev.isxander.controlify.driver;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.debug.DebugProperties;
import org.libsdl.SDL;

public class SDL2GamepadDriver implements GyroDriver, RumbleDriver {
    private final long ptrGamepad;
    private GamepadState.GyroState gyroDelta;
    private final boolean isGyroSupported, isRumbleSupported;

    public SDL2GamepadDriver(int jid) {
        this.ptrGamepad =  SDL.SDL_GameControllerOpen(jid);
        this.isGyroSupported = SDL.SDL_GameControllerHasSensor(ptrGamepad, SDL.SDL_SENSOR_GYRO);
        this.isRumbleSupported = SDL.SDL_GameControllerHasRumble(ptrGamepad);
    }

    @Override
    public void update() {
        if (isGyroSupported()) {
            float[] gyro = new float[3];
            SDL.SDL_GameControllerGetSensorData(ptrGamepad, SDL.SDL_SENSOR_GYRO, gyro, 3);
            gyroDelta = new GamepadState.GyroState(gyro[0], gyro[1], gyro[2]);
            if (DebugProperties.PRINT_GYRO) Controlify.LOGGER.info("Gyro delta: " + gyroDelta);
        }
        SDL.SDL_GameControllerUpdate();
    }

    @Override
    public boolean rumble(float strongMagnitude, float weakMagnitude) {
        // duration of 0 is infinite
        if (!SDL.SDL_GameControllerRumble(ptrGamepad, (int)(strongMagnitude * 65535.0F), (int)(weakMagnitude * 65535.0F), 0)) {
            Controlify.LOGGER.error("Could not rumble controller: " + SDL.SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public GamepadState.GyroState getGyroState() {
        return gyroDelta;
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
    public void close() {
        SDL.SDL_GameControllerClose(ptrGamepad);
    }

    @Override
    public String getGyroDetails() {
        return "SDL2 supported=%s".formatted(isGyroSupported);
    }

    @Override
    public String getRumbleDetails() {
        return "SDL2 supported=%s".formatted(isRumbleSupported);
    }
}
