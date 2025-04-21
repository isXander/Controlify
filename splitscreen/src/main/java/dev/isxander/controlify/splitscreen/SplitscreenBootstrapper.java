package dev.isxander.controlify.splitscreen;

import dev.isxander.controlify.splitscreen.client.protocol.PawnConnectionListener;
import dev.isxander.controlify.splitscreen.server.SplitscreenController;
import dev.isxander.controlify.splitscreen.util.SocketUtil;
import dev.isxander.controlify.utils.Platform;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SplitscreenBootstrapper {
    private static @Nullable SplitscreenController controller;
    private static @Nullable PawnConnectionListener pawnConnectionListener;

    /**
     * When a client is started it has to decide whether
     * to become a controller, or a pawn.
     *
     * It should do this by testing if the socket is
     * already open (a controller exists), becoming a
     * pawn if so, or opening a socket and becoming a
     * controller if not.
     */
    public static void bootstrap(Minecraft minecraft) {
        SocketConnectionMethod connectionMethod = getConnectionMethod()
                .orElseThrow(() -> new RuntimeException("No connection method available"));

        // Attempt to be a pawn (client) first...
        if (SocketUtil.isSocketOpen(connectionMethod)) {
            bootstrapAsPawn(minecraft, connectionMethod);
        } else {
            bootstrapAsController(minecraft, connectionMethod);
        }
    }

    private static void bootstrapAsPawn(Minecraft minecraft, SocketConnectionMethod connectionMethod) {
        pawnConnectionListener = new PawnConnectionListener(minecraft, connectionMethod);
    }

    private static void bootstrapAsController(Minecraft minecraft, SocketConnectionMethod connectionMethod) {
        controller = new SplitscreenController(minecraft, connectionMethod);
    }

    public static boolean isSplitscreen() {
        return controller != null || pawnConnectionListener != null;
    }

    public static Optional<SplitscreenController> getController() {
        return Optional.ofNullable(controller);
    }

    public static Optional<PawnConnectionListener> getPawn() {
        return Optional.ofNullable(pawnConnectionListener);
    }

    public static Optional<Side> getSide() {
        if (controller != null) {
            return Optional.of(Side.CONTROLLER);
        } else if (pawnConnectionListener != null) {
            return Optional.of(Side.PAWN);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<SocketConnectionMethod> getConnectionMethod() {
        if (SocketUtil.isAfUnixSupported()) {
            return switch (Platform.current()) {
                case WINDOWS -> Optional.of(SocketConnectionMethod.Unix.LOCAL_WINDOWS);
                case LINUX, MAC -> Optional.of(SocketConnectionMethod.Unix.LOCAL_UNIX);
                default -> Optional.empty();
            };
        } else {
            return Optional.of(SocketConnectionMethod.TCP.DEFAULT);
        }
    }
}
