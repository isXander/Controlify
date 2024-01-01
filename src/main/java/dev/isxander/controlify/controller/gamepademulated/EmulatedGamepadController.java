package dev.isxander.controlify.controller.gamepademulated;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.controller.gamepad.BuiltinGamepadTheme;
import dev.isxander.controlify.controller.gamepad.GamepadLike;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.controller.gamepademulated.mapping.AxisMapping;
import dev.isxander.controlify.controller.gamepademulated.mapping.ButtonMapping;
import dev.isxander.controlify.controller.gamepademulated.mapping.GamepadMapping;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.driver.gamepad.BasicGamepadState;
import dev.isxander.controlify.driver.joystick.BasicJoystickState;
import dev.isxander.controlify.driver.joystick.JoystickDrivers;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.utils.Log;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class EmulatedGamepadController extends AbstractController<GamepadState, EmulatedGamepadConfig> implements GamepadLike<EmulatedGamepadConfig> {
    private GamepadState state = GamepadState.EMPTY;
    private GamepadState prevState = GamepadState.EMPTY;
    private BasicJoystickState joyState;
    private BasicJoystickState prevJoyState;

    private final RumbleManager rumbleManager;

    public final JoystickDrivers drivers;
    private final Set<Driver> uniqueDrivers;

    public EmulatedGamepadController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        super(joystickId, hidInfo);

        this.drivers = JoystickDrivers.forController(joystickId, hidInfo.hidDevice());
        this.uniqueDrivers = drivers.getUniqueDrivers();
        this.drivers.printDrivers();

        this.joyState = this.prevJoyState = BasicJoystickState.empty(
                drivers.basicJoystickInputDriver().getNumButtons(),
                drivers.basicJoystickInputDriver().getNumAxes(),
                drivers.basicJoystickInputDriver().getNumHats()
        );

        if (!this.name.startsWith(type().friendlyName()))
            setName(this.drivers.nameProviderDriver().getName());

        this.rumbleManager = new RumbleManager(this);

        this.defaultConfig = new EmulatedGamepadConfig();
        this.config = new EmulatedGamepadConfig();

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

    public BasicJoystickState joyState() {
        return joyState;
    }

    public BasicJoystickState prevJoyState() {
        return prevJoyState;
    }

    @Override
    public void updateState() {
        prevState = state;
        prevJoyState = joyState;

        uniqueDrivers.forEach(Driver::update);

        joyState = drivers.basicJoystickInputDriver().getBasicJoystickState();
        GamepadMapping mapping = config.mapping;

        BasicGamepadState basicGamepadState = mapping.mapJoystick(joyState);

        GamepadState.AxesState deadzoneAxesState = basicGamepadState.axes()
                .leftJoystickDeadZone(config.getLeftStickDeadzone())
                .rightJoystickDeadZone(config.getRightStickDeadzone());

        state = new GamepadState(deadzoneAxesState, basicGamepadState.axes(), basicGamepadState.buttons(), new GamepadState.GyroState(), new GamepadState.GyroState());

        if (DebugProperties.PRINT_GAMEPAD_STATE) {
            Log.LOGGER.info(state.toString());
        }
    }

    @Override
    public void clearState() {
        state = GamepadState.EMPTY;
        joyState = BasicJoystickState.EMPTY;
    }

    @Override
    public RumbleManager rumbleManager() {
        return rumbleManager;
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude) {
        if (!supportsRumble() || !config.allowVibrations) return false;

        return drivers.rumbleDriver().rumble(Math.min(strongMagnitude, 1), Math.min(weakMagnitude, 1));
    }

    @Override
    public boolean supportsRumble() {
        return drivers.rumbleDriver().isRumbleSupported();
    }

    @Override
    public ResourceLocation icon() {
        String theme = config().theme == BuiltinGamepadTheme.DEFAULT ? type().themeId() : config().theme.id();
        return Controlify.id("textures/gui/gamepad/" + theme + "/icon.png");
    }

    @Override
    public BatteryLevel batteryLevel() {
        return drivers.batteryDriver().getBatteryLevel();
    }

    @Override
    public boolean supportsGyro() {
        return false;
    }

    @Override
    public GamepadState.GyroStateC gyroState() {
        return GamepadState.GyroStateC.ZERO;
    }

    @Override
    public void close() {
        uniqueDrivers.forEach(Driver::close);
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        gson = gson.newBuilder()
                .registerTypeAdapter(ButtonMapping.class, new ButtonMapping.ButtonMappingSerializer())
                .registerTypeAdapter(AxisMapping.class, new AxisMapping.AxisMappingSerializer())
                .create();
        super.setConfig(gson, json);
        System.out.println(config.mapping);
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

    @Override
    public String kind() {
        return "emulated_gamepad";
    }
}
