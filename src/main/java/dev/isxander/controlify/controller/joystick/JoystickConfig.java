package dev.isxander.controlify.controller.joystick;

import dev.isxander.controlify.controller.ControllerConfig;

import java.util.HashMap;
import java.util.Map;

public class JoystickConfig extends ControllerConfig {
    private Map<String, Float> deadzones;

    private transient JoystickController controller;

    public JoystickConfig(JoystickController controller) {
        setup(controller);
    }

    @Override
    public void setDeadzone(int axis, float deadzone) {
        if (axis < 0)
            throw new IllegalArgumentException("Axis cannot be negative!");

        deadzones.put(controller.mapping().axis(axis).identifier(), deadzone);
    }

    @Override
    public float getDeadzone(int axis) {
        if (axis < 0)
            throw new IllegalArgumentException("Axis cannot be negative!");

        return deadzones.getOrDefault(controller.mapping().axis(axis).identifier(), 0.2f);
    }

    void setup(JoystickController controller) {
        this.controller = controller;
        if (this.deadzones == null) {
            deadzones = new HashMap<>();
            for (int i = 0; i < controller.axisCount(); i++) {
                if (controller.mapping().axis(i).requiresDeadzone())
                    deadzones.put(controller.mapping().axis(i).identifier(), 0.2f);
            }
        }
    }
}
