package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.controller.sdl2.SDL2NativesManager;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import org.libsdl.SDL;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class GamepadController extends AbstractController<GamepadState, GamepadConfig> {
    private GamepadState state = GamepadState.EMPTY;
    private GamepadState prevState = GamepadState.EMPTY;

    private long gamepadPtr;
    private boolean rumbleSupported, triggerRumbleSupported;
    private final RumbleManager rumbleManager;
    private boolean hasGyro;
    private GamepadState.GyroState absoluteGyro = GamepadState.GyroState.ORIGIN;

    public GamepadController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        super(joystickId, hidInfo);
        if (!GLFW.glfwJoystickIsGamepad(joystickId))
            throw new IllegalArgumentException("Joystick " + joystickId + " is not a gamepad!");

        if (!this.name.startsWith(type().friendlyName()))
            setName(GLFW.glfwGetGamepadName(joystickId));

        this.rumbleManager = new RumbleManager(this);

        this.defaultConfig = new GamepadConfig();
        this.config = new GamepadConfig();
    }

    @Override
    public GamepadState state() {
        return state;
    }

    @Override
    public GamepadState prevState() {
        return prevState;
    }

    @Override
    public void updateState() {
        prevState = state;

        GamepadState.AxesState rawAxesState = GamepadState.AxesState.fromController(this);
        GamepadState.AxesState axesState = rawAxesState
                .leftJoystickDeadZone(config().leftStickDeadzoneX, config().leftStickDeadzoneY)
                .rightJoystickDeadZone(config().rightStickDeadzoneX, config().rightStickDeadzoneY);
        GamepadState.ButtonState buttonState = GamepadState.ButtonState.fromController(this);

        GamepadState.GyroState gyroDelta = null;
        if (this.hasGyro) {
            float[] gyro = new float[3];
            SDL.SDL_GameControllerGetSensorData(gamepadPtr, SDL.SDL_SENSOR_GYRO, gyro, 3);
            gyroDelta = new GamepadState.GyroState(gyro[0], gyro[1], gyro[2]);
            if (DebugProperties.PRINT_GYRO) Controlify.LOGGER.info("Gyro delta: " + gyroDelta);
            absoluteGyro = absoluteGyro.add(gyroDelta);
        }
        SDL.SDL_GameControllerUpdate();

        state = new GamepadState(axesState, rawAxesState, buttonState, gyroDelta, absoluteGyro);
    }

    public GamepadState.GyroState absoluteGyroState() {
        return absoluteGyro;
    }

    public boolean hasGyro() {
        return hasGyro;
    }

    @Override
    public void clearState() {
        state = GamepadState.EMPTY;
    }

    public void consumeButtonState() {
        this.state = new GamepadState(state().gamepadAxes(), state().rawGamepadAxes(), GamepadState.ButtonState.EMPTY, state().gyroDelta(), state().absoluteGyroPos());
    }

    GLFWGamepadState getGamepadState() {
        GLFWGamepadState state = GLFWGamepadState.create();
        GLFW.glfwGetGamepadState(joystickId, state);
        return state;
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude, RumbleSource source) {
        if (!canRumble()) return false;

        var strengthMod = config().getRumbleStrength(source);
        if (source != RumbleSource.MASTER)
            strengthMod *= config().getRumbleStrength(RumbleSource.MASTER);

        strongMagnitude *= strengthMod;
        weakMagnitude *= strengthMod;

        // the duration doesn't matter because we are not updating the gamecontroller state,
        // so there is never any SDL check to stop the rumble after the desired time.
        if (!SDL.SDL_GameControllerRumble(gamepadPtr, (int)(strongMagnitude * 65535.0F), (int)(weakMagnitude * 65535.0F), 0)) {
            Controlify.LOGGER.error("Could not rumble controller " + name() + ": " + SDL.SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean canRumble() {
        return rumbleSupported
                && config().allowVibrations
                && ControlifyApi.get().currentInputMode() == InputMode.CONTROLLER;
    }

    @Override
    public RumbleManager rumbleManager() {
        return this.rumbleManager;
    }

    @Override
    public void open() {
        if (SDL2NativesManager.isLoaded()) {
            this.gamepadPtr = SDL.SDL_GameControllerOpen(joystickId);
            Controlify.LOGGER.info(SDL.SDL_GetError());
            this.rumbleSupported = SDL.SDL_GameControllerHasRumble(gamepadPtr);
            this.triggerRumbleSupported = SDL.SDL_GameControllerHasRumble(gamepadPtr);
            if (this.hasGyro = SDL.SDL_GameControllerHasSensor(gamepadPtr, SDL.SDL_SENSOR_GYRO)) {
                SDL.SDL_GameControllerSetSensorEnabled(gamepadPtr, SDL.SDL_SENSOR_GYRO, true);
            }
        } else {
            this.gamepadPtr = 0;
            this.rumbleSupported = false;
            this.hasGyro = false;
        }
    }

    @Override
    public void close() {
        SDL.SDL_GameControllerClose(gamepadPtr);
        this.gamepadPtr = 0;
        this.rumbleSupported = false;
        this.hasGyro = false;
    }
}
