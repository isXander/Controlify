package dev.isxander.controlify.splitscreen;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for a splitscreen pawn.
 * <p>
 * <ul>
 *     <li>If ran on the server, the pawn instance can controls the self-client, or send packets to control remote pawns.</li>
 *     <li>If ran on the client, will control itself.</li>
 * </ul>
 * <p>
 * <strong>Unless otherwise stated, methods should expect to be run on the main thread.</strong>
 */
public interface SplitscreenPawn extends Bridge {
    /**
     * Connects client to the given server.
     *
     * @param serverAddress address of the server
     * @param serverPort port of the server
     */
    void joinServer(String serverAddress, int serverPort);

    /**
     * Closes the game.
     */
    void closeGame();

    /**
     * @return the current splitscreen mode of the window
     */
    SplitscreenPosition getWindowSplitscreenMode();

    /**
     * Tells the pawn to use the given controller.
     * @param controllerUid the UID of the controller to use
     */
    void useController(ControllerUID controllerUid);

    /**
     * @return the associated controller UID for this pawn, or null if no controller is associated
     */
    @Nullable ControllerUID getAssociatedController();
}
