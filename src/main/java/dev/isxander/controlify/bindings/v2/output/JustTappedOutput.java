package dev.isxander.controlify.bindings.v2.output;

import dev.isxander.controlify.bindings.v2.InputBinding;
import dev.isxander.controlify.bindings.v2.StateAccess;

public class JustTappedOutput implements DigitalOutput {
    public static final int DEFAULT_MAX_HOLD_TIME_TICKS = 2;

    private final StateAccess stateAccess;
    private final int maxHoldTime;

    public JustTappedOutput(InputBinding binding, int maxHoldTimeTicks) {
        this.stateAccess = binding.createStateAccess(maxHoldTimeTicks + 2);
        this.maxHoldTime = maxHoldTimeTicks;
    }

    public JustTappedOutput(InputBinding binding) {
        this(binding, DEFAULT_MAX_HOLD_TIME_TICKS);
    }

    @Override
    public boolean get() {
        if (stateAccess.isSuppressed())
            return false;

        boolean justReleased = stateAccess.digital(0);
        if (!justReleased) return false; // still holding or just started to hold, not just tapped

        for (int i = 1; i < maxHoldTime + 2; i++) { // loop through remaining history
            boolean state = stateAccess.digital(i);
            if (!state) { // if we find one that isn't pressed, we know immediately a tap has taken place
                return true;
            }
        }

        // if we reached the end of the loop without return,
        // we know that the maxHoldTime has been exceeded and we did not just tap
        return false;
    }
}
