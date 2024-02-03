package dev.isxander.controlify.driver.joystick;

import dev.isxander.controlify.controller.composable.HatState;

public record BasicJoystickState(boolean[] buttons, float[] axes, HatState[] hats) {
    public static final BasicJoystickState EMPTY = new BasicJoystickState(new boolean[0], new float[0], new HatState[0]);

    public static BasicJoystickState empty(int numButtons, int numAxes, int numHats) {
        return new BasicJoystickState(new boolean[numButtons], new float[numAxes], new HatState[numHats]);
    }
}
