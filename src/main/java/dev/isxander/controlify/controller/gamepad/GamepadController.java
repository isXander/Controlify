package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.driver.*;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

public class GamepadController extends AbstractController<GamepadState, GamepadConfig> {
    private GamepadState state = GamepadState.EMPTY;
    private GamepadState prevState = GamepadState.EMPTY;

    private final RumbleManager rumbleManager;
    private GamepadState.GyroState absoluteGyro = GamepadState.GyroState.ORIGIN;

    private final GamepadDrivers drivers;
    private final Set<Driver> uniqueDrivers;

    public GamepadController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        super(joystickId, hidInfo);
        if (!GLFW.glfwJoystickIsGamepad(joystickId))
            throw new IllegalArgumentException("Joystick " + joystickId + " is not a gamepad!");

        if (!this.name.startsWith(type().friendlyName()))
            setName(GLFW.glfwGetGamepadName(joystickId));

        this.drivers = GamepadDrivers.forController(joystickId, hidInfo.hidDevice());
        this.uniqueDrivers = drivers.getUniqueDrivers();
        this.drivers.printDrivers();

        this.rumbleManager = new RumbleManager(this);

        this.defaultConfig = new GamepadConfig();
        this.config = new GamepadConfig();

        this.bindings = new ControllerBindings<>(this);
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

        uniqueDrivers.forEach(Driver::update);

        BasicGamepadInputDriver.BasicGamepadState basicState = drivers.basicGamepadInputDriver().getBasicGamepadState();
        GamepadState.AxesState deadzoneAxesState = basicState.axes()
                .leftJoystickDeadZone(config().leftStickDeadzoneX, config().leftStickDeadzoneY)
                .rightJoystickDeadZone(config().rightStickDeadzoneX, config().rightStickDeadzoneY);

        GamepadState.GyroState gyroState = drivers.gyroDriver().getGyroState();

        state = new GamepadState(deadzoneAxesState, basicState.axes(), basicState.buttons(), gyroState, absoluteGyro);
    }

    public GamepadState.GyroState absoluteGyroState() {
        return absoluteGyro;
    }

    public boolean hasGyro() {
        return drivers.gyroDriver().isGyroSupported();
    }

    @Override
    public void clearState() {
        state = GamepadState.EMPTY;
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude, RumbleSource source) {
        if (!supportsRumble() || !config().allowVibrations) return false;

        var strengthMod = config().getRumbleStrength(source);
        if (source != RumbleSource.MASTER)
            strengthMod *= config().getRumbleStrength(RumbleSource.MASTER);

        strongMagnitude *= strengthMod;
        weakMagnitude *= strengthMod;

        return drivers.rumbleDriver().rumble(Math.min(strongMagnitude, 1), Math.min(weakMagnitude, 1));
    }

    @Override
    public boolean supportsRumble() {
        return drivers.rumbleDriver().isRumbleSupported();
    }

    @Override
    public RumbleManager rumbleManager() {
        return this.rumbleManager;
    }

    @Override
    public BatteryLevel batteryLevel() {
        return drivers.batteryDriver().getBatteryLevel();
    }

    @Override
    public void close() {
        uniqueDrivers.forEach(Driver::close);
    }

    @Override
    public ResourceLocation icon() {
        String theme = config().theme == BuiltinGamepadTheme.DEFAULT ? type().themeId() : config().theme.id();
        return Controlify.id("textures/gui/gamepad/" + theme + "/icon.png");
    }
}
