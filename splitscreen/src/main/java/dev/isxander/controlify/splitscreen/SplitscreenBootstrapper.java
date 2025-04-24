package dev.isxander.controlify.splitscreen;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.remote.ipc.PawnConnectionListener;
import dev.isxander.controlify.splitscreen.ipc.IPCMethod;
import dev.isxander.controlify.splitscreen.host.SplitscreenController;
import dev.isxander.controlify.splitscreen.util.SocketUtil;
import dev.isxander.controlify.utils.Platform;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * The main class of the splitscreen mod.
 * Valid on both host and pawns, faciliates the abstraction and access
 * to the controller and pawn main classes.
 */
public class SplitscreenBootstrapper {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static @Nullable SplitscreenController controller;
    private static @Nullable PawnConnectionListener pawnConnectionListener;

    /**
     * When a client is started it has to decide whether
     * to become a controller, or a pawn.
     * <p>
     * It should do this by testing if the socket is
     * already open (a controller exists), becoming a
     * pawn if so, or opening a socket and becoming a
     * controller if not.
     */
    public static void bootstrap(Minecraft minecraft) {
        LOGGER.info("Boostrapping Controlify splitscreen!");

        IPCMethod connectionMethod = determineIPCMethod()
                .orElseThrow(() -> new RuntimeException("No connection method available"));

        // Attempt to be a pawn (client) first...
        if (SocketUtil.isSocketListening(connectionMethod)) {
            LOGGER.info("Socket already open, attempting to become a pawn");
            bootstrapAsPawn(minecraft, connectionMethod);
        } else {
            LOGGER.info("Socket not open, attempting to become a controller");
            bootstrapAsController(minecraft, connectionMethod);
        }
    }

    private static void bootstrapAsPawn(Minecraft minecraft, IPCMethod connectionMethod) {
        pawnConnectionListener = new PawnConnectionListener(minecraft, connectionMethod);
    }

    private static void bootstrapAsController(Minecraft minecraft, IPCMethod connectionMethod) {
        controller = new SplitscreenController(minecraft, connectionMethod);
    }

    public static boolean isSplitscreen() {
        return controller != null || pawnConnectionListener != null;
    }

    /**
     * Valid on both controller and pawn, empty if splitscreen is not active
     * @return the controller bridge
     */
    public static Optional<ControllerBridge> getControllerBridge() {
        return getController().<ControllerBridge>map(SplitscreenController::getControllerBridge)
                .or(() -> getPawn().<ControllerBridge>map(PawnConnectionListener::getControllerBridge));
    }

    /**
     * @return the controller if on host, otherwise empty
     */
    public static Optional<SplitscreenController> getController() {
        return Optional.ofNullable(controller);
    }

    /**
     * @return the pawn connection listener if a pawn, otherwise empty
     */
    public static Optional<PawnConnectionListener> getPawn() {
        return Optional.ofNullable(pawnConnectionListener);
    }

    /**
     * @return the side of the connection, either controller or pawn, empty if splitscreen is not active
     */
    public static Optional<Side> getSide() {
        if (controller != null) {
            return Optional.of(Side.CONTROLLER);
        } else if (pawnConnectionListener != null) {
            return Optional.of(Side.PAWN);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<IPCMethod> determineIPCMethod() {
        if (SocketUtil.isAfUnixSupported() && false) {
            return switch (Platform.current()) {
                case WINDOWS -> Optional.of(IPCMethod.Unix.LOCAL_WINDOWS);
                case LINUX, MAC -> Optional.of(IPCMethod.Unix.LOCAL_UNIX);
                default -> Optional.empty();
            };
        } else {
            return Optional.of(IPCMethod.TCP.DEFAULT);
        }
    }
}
