package dev.isxander.controlify.splitscreen.engine.impl.reparenting;

import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import net.minecraft.client.Minecraft;

public class LocalReparentingPawn implements ReparentingPawn {

    private final Minecraft minecraft;

    private final NativeWindowHandle windowHandle;

    public LocalReparentingPawn(Minecraft minecraft, NativeWindowHandle windowHandle) {
        this.minecraft = minecraft;
        this.windowHandle = windowHandle;
    }

    @Override
    public void setWindowFocusState(boolean active) {
        this.minecraft.setWindowActive(active);
    }

    @Override
    public NativeWindowHandle getNativeWindowHandle() {
        return this.windowHandle;
    }
}
