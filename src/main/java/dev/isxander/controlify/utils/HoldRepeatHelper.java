package dev.isxander.controlify.utils;

import dev.isxander.controlify.api.bind.InputBinding;

public class HoldRepeatHelper {
    private final int initialDelay, repeatDelay;
    private int currentDelay;

    private boolean hasResetThisTick = false;

    public HoldRepeatHelper(int initialDelay, int repeatDelay) {
        this.initialDelay = initialDelay;
        this.repeatDelay = repeatDelay;
        this.currentDelay = 0;
    }

    public boolean canNavigate() {
        return --currentDelay <= 0;
    }

    public void reset() {
        currentDelay = initialDelay;
        hasResetThisTick = true;
    }

    public void clearDelay() {
        currentDelay = 0;
    }

    public void onNavigate() {
        if (!hasResetThisTick) {
            currentDelay = repeatDelay;
        } else {
            hasResetThisTick = false;
        }
    }

    public boolean shouldAction(InputBinding binding) {
        boolean shouldAction = binding.digitalNow() && (canNavigate() || !binding.digitalPrev());
        if (shouldAction && !binding.digitalPrev()) {
            reset();
        }
        return shouldAction;
    }
}
