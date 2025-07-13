package dev.isxander.splitscreen.client.engine;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.SplitscreenPosition;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ReparentingHostSplitscreenEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The engine to achieve a splitscreen effect.
 */
public interface HostSplitscreenEngine extends SplitscreenEngine {

    static HostSplitscreenEngine create(Minecraft minecraft, InputMethod localInputMethod) {
        return new ReparentingHostSplitscreenEngine(minecraft, localInputMethod);
    }


    /**
     * Sets the splitscreen mode of the given window.
     * @param window the window to set the splitscreen mode for
     * @param position the position of the splitscreen
     */
    void setSplitscreenMode(InputMethod window, SplitscreenPosition position);

    void removeWindow(InputMethod window);

    /**
     * Notifies that the engine requires the controller to refresh the splitscreen mode
     * of all the pawns.
     */
    boolean isDirty();

    boolean consumeDirty();

    /**
     * Notifies that the whole game should exit.
     */
    boolean shouldExit();

    void handleInboundPayload(InputMethod window, Connection connection, CustomPacketPayload payload);
}
