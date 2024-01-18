package dev.isxander.controlify.controller.joystick;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.RPJoystickMapping;
import dev.isxander.controlify.rumble.RumbleCapable;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Log;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class CompoundJoystickController implements JoystickController<JoystickConfig>, RumbleCapable {
    private final String uid;
    private final List<Integer> joysticks;
    private final int axisCount, buttonCount, hatCount;
    private final ControllerType compoundType;
    private final ControllerBindings<JoystickState> bindings;
    private final JoystickMapping mapping;

    private JoystickConfig config;
    private final JoystickConfig defaultConfig;

    private final RumbleManager rumbleManager;

    private JoystickState state = JoystickState.EMPTY, prevState = JoystickState.EMPTY;

    public CompoundJoystickController(List<Integer> joystickIds, String uid, ControllerType compoundType) {
        this.joysticks = ImmutableList.copyOf(joystickIds);
        this.uid = uid;
        this.compoundType = compoundType;

        this.axisCount = joystickIds.stream().mapToInt(this::getAxisCountForJoystick).sum();
        this.buttonCount = joystickIds.stream().mapToInt(this::getButtonCountForJoystick).sum();
        this.hatCount = joystickIds.stream().mapToInt(this::getHatCountForJoystick).sum();

        this.mapping = RPJoystickMapping.fromType(this);

        this.config = new JoystickConfig(this);
        this.defaultConfig = new JoystickConfig(this);

        this.rumbleManager = new RumbleManager(this);

        this.bindings = new ControllerBindings<>(this);
    }


    @Override
    public String uid() {
        return this.uid;
    }

    @Override
    public ControllerBindings<JoystickState> bindings() {
        return this.bindings;
    }

    @Override
    public JoystickState state() {
        return this.state;
    }

    @Override
    public JoystickState prevState() {
        return this.prevState;
    }

    @Override
    public void updateState() {
        this.prevState = this.state;

        var states = this.joysticks.stream().map(joystick -> JoystickState.fromJoystick(this, joystick)).toList();
        this.state = JoystickState.merged(mapping(), states);
    }

    @Override
    public void clearState() {
        this.state = JoystickState.empty(this);
    }

    @Override
    public JoystickConfig config() {
        return this.config;
    }

    @Override
    public JoystickConfig defaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public void resetConfig() {
        this.config = new JoystickConfig(this);
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        JoystickConfig newConfig = gson.fromJson(json, JoystickConfig.class);
        if (newConfig != null) {
            this.config = newConfig;
        } else {
            Log.LOGGER.error("Could not set config for controller " + name() + " (" + uid() + ")! Using default config instead.");
            this.config = defaultConfig();
        }
        this.config.setup(this);
    }

    @Override
    public ControllerType type() {
        return this.compoundType;
    }

    @Override
    public String name() {
        return type().friendlyName();
    }

    @Override
    public JoystickMapping mapping() {
        return this.mapping;
    }

    @Override
    public int axisCount() {
        return this.axisCount;
    }

    @Override
    public int buttonCount() {
        return this.buttonCount;
    }

    @Override
    public int hatCount() {
        return this.hatCount;
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude) {
        return false;
    }

    @Override
    public boolean supportsRumble() {
        return false;
    }

    @Override
    public RumbleState applyRumbleSourceStrength(RumbleState state, RumbleSource source) {
        return state;
    }

    @Override
    public RumbleManager rumbleManager() {
        return this.rumbleManager;
    }

    @Override
    public boolean canBeUsed() {
        return JoystickController.super.canBeUsed()
                && joysticks.stream().allMatch(GLFW::glfwJoystickPresent);
    }

    @Override
    public int joystickId() {
        return -1;
    }

    private int getAxisCountForJoystick(int joystick) {
        return GLFW.glfwGetJoystickAxes(joystick).capacity();
    }

    private int getButtonCountForJoystick(int joystick) {
        return GLFW.glfwGetJoystickButtons(joystick).capacity();
    }

    private int getHatCountForJoystick(int joystick) {
        return GLFW.glfwGetJoystickHats(joystick).capacity();
    }

    @Override
    public Optional<ControllerHIDService.ControllerHIDInfo> hidInfo() {
        return Optional.empty();
    }

    @Override
    public String kind() {
        return "compound_joystick";
    }
}
