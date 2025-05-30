package dev.isxander.splitscreen.engine.impl.reparenting;

import dev.isxander.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;

public interface ReparentingPawn {
    NativeWindowHandle getNativeWindowHandle();

    void setWindowFocusState(boolean active);

    void setThrottleFramerate(boolean throttle);
}
