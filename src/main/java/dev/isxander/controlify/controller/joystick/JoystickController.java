package dev.isxander.controlify.controller.joystick;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.controller.joystick.mapping.RPJoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class JoystickController extends AbstractController<JoystickState, JoystickConfig> {
    private JoystickState state = JoystickState.EMPTY, prevState = JoystickState.EMPTY;
    private final int axisCount, buttonCount, hatCount;
    private final JoystickMapping mapping;

    public JoystickController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        super(joystickId, hidInfo);

        this.axisCount = GLFW.glfwGetJoystickAxes(joystickId).capacity();
        this.buttonCount = GLFW.glfwGetJoystickButtons(joystickId).capacity();
        this.hatCount = GLFW.glfwGetJoystickHats(joystickId).capacity();

        this.mapping = Objects.requireNonNull(RPJoystickMapping.fromType(type()));
        
        this.config = new JoystickConfig(this);
        this.defaultConfig = new JoystickConfig(this);
    }

    @Override
    public JoystickState state() {
        return state;
    }

    @Override
    public JoystickState prevState() {
        return prevState;
    }

    @Override
    public void updateState() {
        prevState = state;
        state = JoystickState.fromJoystick(this, joystickId);
    }

    public JoystickMapping mapping() {
        return mapping;
    }

    @Override
    public boolean canBeUsed() {
        return !(mapping() instanceof UnmappedJoystickMapping);
    }

    public int axisCount() {
        return axisCount;
    }

    public int buttonCount() {
        return buttonCount;
    }

    public int hatCount() {
        return hatCount;
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        super.setConfig(gson, json);
        this.config.setup(this);
    }
}
