package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.driver.gamepad.BasicGamepadInputDriver;
import dev.isxander.controlify.driver.gamepad.GamepadDrivers;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.*;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.Log;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

public class GamepadController extends AbstractController<GamepadState, GamepadConfig> {
    private GamepadState state = GamepadState.EMPTY;
    private GamepadState prevState = GamepadState.EMPTY;

    private final RumbleManager rumbleManager;
    private GamepadState.GyroState absoluteGyro = new GamepadState.GyroState();

    public final GamepadDrivers drivers;
    private final Set<Driver> uniqueDrivers;

    private int antiSnapbackTicksL, antiSnapbackTicksR;

    public GamepadController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        super(joystickId, hidInfo);
        if (!GLFW.glfwJoystickIsGamepad(joystickId))
            throw new IllegalArgumentException("Joystick " + joystickId + " is not a gamepad!");

        this.drivers = GamepadDrivers.forController(joystickId, hidInfo.hidDevice());
        this.uniqueDrivers = drivers.getUniqueDrivers();
        this.drivers.printDrivers();

        if (!this.name.startsWith(type().friendlyName()))
            setName(this.drivers.nameProviderDriver().getName());

        this.rumbleManager = new RumbleManager(this);

        this.defaultConfig = new GamepadConfig();
        this.config = new GamepadConfig();
        if (hidInfo.hidDevice().map(hid -> SteamDeckDriver.isSteamDeck(hid.vendorID(), hid.productID())).orElse(false)) {
            this.defaultConfig.mixedInput = true;
            this.config.mixedInput = true;
        }

        this.bindings = new ControllerBindings<>(this);

        this.config.validateRadialActions(bindings);
        this.defaultConfig.validateRadialActions(bindings);
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

        if (DebugProperties.PRINT_GAMEPAD_STATE) {
            Log.LOGGER.info(basicState.toString());
        }

        GamepadState.AxesState deadzoneAxesState = basicState.axes()
                .leftJoystickDeadZone(config().getLeftStickDeadzone())
                .rightJoystickDeadZone(config().getRightStickDeadzone());

        if (DebugProperties.USE_SNAPBACK) {
            if (antiSnapbackTicksL > 0) {
                deadzoneAxesState = deadzoneAxesState.neutraliseLeft();
                antiSnapbackTicksL--;
            } else if (ControllerUtils.shouldApplyAntiSnapBack(deadzoneAxesState.leftStickX(), deadzoneAxesState.leftStickY(), prevState.gamepadAxes().leftStickX(), prevState.gamepadAxes().leftStickY(), 0.08f)) {
                antiSnapbackTicksL = 2;
                deadzoneAxesState = deadzoneAxesState.neutraliseLeft();
            }
            if (antiSnapbackTicksR > 0) {
                deadzoneAxesState = deadzoneAxesState.neutraliseRight();
                antiSnapbackTicksR--;
            } else if (ControllerUtils.shouldApplyAntiSnapBack(deadzoneAxesState.rightStickX(), deadzoneAxesState.rightStickY(), prevState.gamepadAxes().rightStickX(), prevState.gamepadAxes().rightStickY(), 0.08f)) {
                antiSnapbackTicksR = 2;
                deadzoneAxesState = deadzoneAxesState.neutraliseRight();
            }
        }

        // TODO: Add some sort of gyro filtering
        GamepadState.GyroState gyroState = new GamepadState.GyroState(drivers.gyroDriver().getGyroState()).sub(config().gyroCalibration);
        this.absoluteGyro.add(gyroState);

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
    public boolean setRumble(float strongMagnitude, float weakMagnitude) {
        if (!supportsRumble() || !config().allowVibrations) return false;

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

    @Override
    public int axisCount() {
        return 6;
    }

    @Override
    public int buttonCount() {
        return 15;
    }

    @Override
    public int hatCount() {
        return 0;
    }
}
