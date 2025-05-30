package dev.isxander.splitscreen;

import dev.isxander.controlify.controller.ControllerUID;
import net.minecraft.resources.ResourceLocation;
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
     * the ID of the pawn
     */
    int pawnIndex();

    /**
     * Connects client to the given server.
     *
     * @param serverAddress address of the server
     * @param serverPort    port of the server
     * @param nonce the nonce to use for the connection, or null if not applicable
     */
    void joinServer(String serverAddress, int serverPort, byte @Nullable [] nonce);

    /**
     * Closes the game.
     */
    void closeGame();

    /**
     * Tells the pawn to disconnect from the server it's currently connected to.
     */
    void disconnectFromServer();

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
     * Tells the pawns to load a specific config as it has been saved on the controller.
     * @param config the id of the config that was saved
     */
    void onConfigSave(ResourceLocation config);

    /**
     * @return the associated controller UID for this pawn, or null if no controller is associated
     */
    @Nullable ControllerUID getAssociatedController();
}
