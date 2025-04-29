package dev.isxander.controlify.splitscreen.engine.impl.reparenting;

import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;

public interface ReparentingPawn {
    NativeWindowHandle getNativeWindowHandle();

    void setWindowFocusState(boolean active);
}
