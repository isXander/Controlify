package dev.isxander.controlify.bindings.output;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.StateAccess;

public class LongPressedOutput implements DigitalOutput {
    public static final int DEFAULT_PRESS_DURATION_TICKS = 10;

    private final StateAccess stateAccess;
    private final int minHoldDurationTicks;
    private boolean consumed;

    public LongPressedOutput(InputBinding binding, int pressDurationTicks) {
        this.stateAccess = binding.createStateAccess(pressDurationTicks);
        this.minHoldDurationTicks = pressDurationTicks;
        this.consumed = false;
    }

    public LongPressedOutput(InputBinding binding) {
        this(binding, DEFAULT_PRESS_DURATION_TICKS);
    }

    @Override
    public boolean get() {
        if (stateAccess.isSuppressed())
            return false;

        for (int i = 0; i < minHoldDurationTicks; i++) {
            if (!stateAccess.digital(i)) {
                if (i == 0) consumed = false;
                return false;
            }
        }
        return !consumed;
    }

    public void consume() {
        consumed = true;
    }
}
