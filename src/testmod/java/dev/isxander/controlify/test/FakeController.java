package dev.isxander.controlify.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.controller.joystick.JoystickConfig;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import dev.isxander.controlify.rumble.RumbleCapable;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;

import java.util.List;

public class FakeController implements JoystickController<JoystickConfig> {
    public static int JOYSTICK_COUNT = 0;

    private final String uid;
    private final int id;
    private final ControllerBindings<JoystickState> bindings;
    private final JoystickConfig config;
    private JoystickState state = JoystickState.EMPTY, prevState = JoystickState.EMPTY;
    private final RumbleManager rumbleManager;

    private float axisState;
    private boolean shouldClearAxisNextTick;
    private boolean buttonState, shouldButtonPressNextTick;
    private JoystickState.HatState hatState = JoystickState.HatState.CENTERED;
    private boolean shouldCenterHatNextTick;

    public FakeController() {
        this.uid = "FAKE-" + JOYSTICK_COUNT++;
        this.id = -JOYSTICK_COUNT;
        this.bindings = new ControllerBindings<>(this);
        this.config = new JoystickConfig(this);
        this.rumbleManager = new RumbleManager(new RumbleCapable() {
            @Override
            public boolean setRumble(float strongMagnitude, float weakMagnitude, RumbleSource source) {
                return false;
            }

            @Override
            public boolean canRumble() {
                return false;
            }
        });
        this.config.calibrated = true;
    }

    @Override
    public String uid() {
        return uid;
    }

    @Override
    public int joystickId() {
        return id;
    }

    @Override
    public ControllerBindings<JoystickState> bindings() {
        return bindings;
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
    public JoystickConfig config() {
        return config;
    }

    @Override
    public JoystickConfig defaultConfig() {
        return config;
    }

    @Override
    public void resetConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ControllerType type() {
        return ControllerType.UNKNOWN;
    }

    @Override
    public String name() {
        return "Fake Controller";
    }

    @Override
    public void updateState() {
        buttonState = shouldButtonPressNextTick;
        shouldButtonPressNextTick = false;

        state = new FakeControllerState(mapping(), axisState, buttonState, hatState);

        if (shouldClearAxisNextTick) {
            shouldClearAxisNextTick = false;
            axisState = 0f;
        }
        if (shouldCenterHatNextTick) {
            shouldCenterHatNextTick = false;
            hatState = JoystickState.HatState.CENTERED;
        }
    }

    @Override
    public void clearState() {
        state = JoystickState.EMPTY;
    }

    @Override
    public RumbleManager rumbleManager() {
        return rumbleManager;
    }

    @Override
    public boolean canRumble() {
        return false;
    }

    @Override
    public JoystickMapping mapping() {
        return UnmappedJoystickMapping.EMPTY;
    }

    @Override
    public int axisCount() {
        return 1;
    }

    @Override
    public int buttonCount() {
        return 1;
    }

    @Override
    public int hatCount() {
        return 1;
    }

    public void setAxis(float axis, boolean clearNextTick) {
        this.axisState = axis;
        this.shouldClearAxisNextTick = clearNextTick;
    }

    public void clearAxisNextTick() {
        this.shouldClearAxisNextTick = true;
    }

    public void pressButtonNextTick() {
        this.shouldButtonPressNextTick = true;
    }

    public void setHat(JoystickState.HatState hatState, boolean clearNextTick) {
        this.hatState = hatState;
        this.shouldCenterHatNextTick = clearNextTick;
    }

    public void clearHatNextTick() {
        this.shouldCenterHatNextTick = true;
    }

    public void use() {
        Controlify.instance().setCurrentController(this);
    }

    public void finish() {
        Controlify.instance().setCurrentController(null);
        Controller.CONTROLLERS.remove(uid, this);
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }

    @Override
    public void close() {
        JoystickController.super.close();
    }

    public static class FakeControllerState extends JoystickState {
        protected FakeControllerState(JoystickMapping mapping, float axis, boolean button, HatState hat) {
            super(mapping, List.of(axis), List.of(axis), List.of(button), List.of(hat));
        }
    }
}
