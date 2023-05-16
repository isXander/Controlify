package dev.isxander.controlify.utils;

import dev.isxander.controlify.api.bind.ControllerBinding;

public class NavigationHelper {
    private final int initialDelay, repeatDelay;
    private int currentDelay;

    private boolean hasResetThisTick = false;

    public NavigationHelper(int initialDelay, int repeatDelay) {
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

    public boolean shouldAction(ControllerBinding binding) {
        boolean shouldAction = binding.held() && (canNavigate() || !binding.prevHeld());
        if (shouldAction && !binding.prevHeld()) {
            reset();
        }
        return shouldAction;
    }
}
