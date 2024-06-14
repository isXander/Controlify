package dev.isxander.controlify.bindings.output;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.StateAccess;

public class GuiPressOutput implements DigitalOutput {
    private final StateAccess stateAccess;

    private PressState pressState = PressState.OFF;

    public GuiPressOutput(InputBinding binding) {
        this.stateAccess = binding.createStateAccess(2, state -> push());
    }

    @Override
    public boolean get() {
        return pressState == PressState.JUST_RELEASED;
    }

    public PressState getPressState() {
        return this.pressState;
    }

    private void push() {
        boolean held = stateAccess.digital(0);
        boolean prevHeld = stateAccess.digital(1);

        // if just pressed, set state to could press in future
        // if just released:
        //    if it could press in future, set to just released - this is the state where get() == true
        //    if the state is anything else, set the state to off

        if (held) {
            if (!prevHeld) { // just started pressing
                pressState = PressState.COULD_PRESS_IN_FUTURE;
            }
        } else {
            if (prevHeld && pressState == PressState.COULD_PRESS_IN_FUTURE) { // just released
                pressState = PressState.JUST_RELEASED;
            } else {
                pressState = PressState.OFF;
            }
        }

        if (stateAccess.isSuppressed())
            pressState = PressState.OFF;
    }

    public void onNavigate() {
        this.pressState = PressState.OFF;
    }

    public enum PressState {
        /** Not pressed or navigated since held */
        OFF,
        /** This is the state where the action takes place */
        JUST_RELEASED,
        /** Currently held down with no navigations since it was pressed */
        COULD_PRESS_IN_FUTURE,
    }
}
