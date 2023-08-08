package dev.isxander.controlify.reacharound;

import java.util.function.Function;

public enum ReachAroundPolicy {
    ALLOWED(mode -> true),
    DISALLOWED(mode -> false),
    UNSET(ReachAroundMode::canReachAround);

    private final Function<ReachAroundMode, Boolean> canReachAround;

    ReachAroundPolicy(Function<ReachAroundMode, Boolean> canReachAround) {
        this.canReachAround = canReachAround;
    }

    public boolean canReachAround(ReachAroundMode mode) {
        return canReachAround.apply(mode);
    }

    public static ReachAroundPolicy fromServer(boolean allowed) {
        return allowed ? ALLOWED : DISALLOWED;
    }
}
