package dev.isxander.controlify.splitscreen.engine;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ReparentingHostSplitscreenEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The engine to achieve a splitscreen effect.
 */
public interface HostSplitscreenEngine extends SplitscreenEngine {

    static HostSplitscreenEngine create(Minecraft minecraft, ControllerUID localController) {
        return new ReparentingHostSplitscreenEngine(minecraft, localController);
    }


    /**
     * Sets the splitscreen mode of the given window.
     * @param window the window to set the splitscreen mode for
     * @param position the position of the splitscreen
     */
    void setSplitscreenMode(ControllerUID window, SplitscreenPosition position);

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

    void handleInboundPayload(ControllerUID window, Connection connection, CustomPacketPayload payload);


}
