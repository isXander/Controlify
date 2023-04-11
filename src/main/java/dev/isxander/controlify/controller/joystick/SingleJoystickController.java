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

public class SingleJoystickController extends AbstractController<JoystickState, JoystickConfig> implements JoystickController<JoystickConfig> {
    private JoystickState state = JoystickState.EMPTY, prevState = JoystickState.EMPTY;
    private final JoystickMapping mapping;

    public SingleJoystickController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        super(joystickId, hidInfo);

        this.mapping = Objects.requireNonNull(RPJoystickMapping.fromType(this));
        
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

    @Override
    public void clearState() {
        this.state = JoystickState.empty(this);
    }

    @Override
    public JoystickMapping mapping() {
        return mapping;
    }

    @Override
    public int axisCount() {
        return mapping().axes().length;
    }

    @Override
    public int buttonCount() {
        return mapping.buttons().length;
    }

    @Override
    public int hatCount() {
        return mapping.hats().length;
    }

    @Override
    public int joystickId() {
        return joystickId;
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        super.setConfig(gson, json);
        this.config.setup(this);
    }
}
