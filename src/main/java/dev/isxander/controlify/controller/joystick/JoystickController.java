package dev.isxander.controlify.controller.joystick;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.joystick.mapping.DataJoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import org.hid4java.HidDevice;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class JoystickController extends AbstractController<JoystickState, JoystickConfig> {
    private JoystickState state = JoystickState.EMPTY, prevState = JoystickState.EMPTY;
    private final int axisCount, buttonCount, hatCount;
    private final JoystickMapping mapping;

    public JoystickController(int joystickId, @Nullable HidDevice hidDevice) {
        super(joystickId, hidDevice);

        this.axisCount = GLFW.glfwGetJoystickAxes(joystickId).capacity();
        this.buttonCount = GLFW.glfwGetJoystickButtons(joystickId).capacity();
        this.hatCount = GLFW.glfwGetJoystickHats(joystickId).capacity();

        this.mapping = Objects.requireNonNull(DataJoystickMapping.fromType(type()));
        
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
        state = JoystickState.fromJoystick(this);
    }

    public JoystickMapping mapping() {
        return mapping;
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
        this.config.setController(this);
    }
}
