package dev.isxander.splitscreen.client;

import com.mojang.logging.LogUtils;
import dev.isxander.splitscreen.client.config.SplitscreenConfig;
import dev.isxander.splitscreen.client.engine.SplitscreenEngine;
import dev.isxander.splitscreen.server.SplitscreenSSClient;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchArguments;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchException;
import dev.isxander.splitscreen.client.remote.RemotePawnMain;
import dev.isxander.splitscreen.client.ipc.IPCMethod;
import dev.isxander.splitscreen.client.host.SplitscreenController;
import dev.isxander.splitscreen.client.features.screenop.ScreenSplitscreenModeRegistry;
import dev.isxander.splitscreen.client.util.SocketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.HttpUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The main class of the splitscreen mod.
 * Valid on both host and pawns, faciliates the abstraction and access
 * to the controller and pawn main classes.
 */
public class SplitscreenBootstrapper {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static @Nullable SplitscreenController controller;
    private static @Nullable RemotePawnMain remotePawnMain;

    /**
     * When a client is started it has to decide whether
     * to become a controller, or a pawn.
     * <p>
     * It should do this by testing if the socket is
     * already open (a controller exists), becoming a
     * pawn if so, or opening a socket and becoming a
     * controller if not.
     */
    // TODO: bootstrap late when a second controller is added, not at init
    public static void bootstrap(Minecraft minecraft) {
        LOGGER.info("Boostrapping Controlify splitscreen!");

        ScreenSplitscreenModeRegistry.init();
        SplitscreenConfig.INSTANCE.loadFromFile();

        boolean relaunched = RelaunchArguments.RELAUNCHED.get().orElse(false);
        if (relaunched) {
            RelaunchArguments.ARGFILE_PATH.get().ifPresent(pathString -> {
                Path path = Path.of(pathString);
                try {
                    Files.delete(path);
                } catch (Exception e) {
                    LOGGER.error("Failed to delete arg file", e);
                }
            });

            int port = RelaunchArguments.IPC_TCP_PORT.get().orElse(-1);
            String socketPath = RelaunchArguments.IPC_SOCKET_PATH.get().orElse(null);

            IPCMethod ipcMethod;
            if (socketPath != null) {
                ipcMethod = new IPCMethod.Unix(socketPath);
            } else if (port != -1) {
                ipcMethod = new IPCMethod.TCP(port);
            } else {
                throw new RelaunchException("No socket path or TCP port provided");
            }

            int pawnIndex = RelaunchArguments.PAWN_INDEX.get().orElseThrow();

            LOGGER.info("Detected relaunch, becoming pawn#{} and connecting to controller via TCP at port {}", pawnIndex, port);
            bootstrapAsPawn(minecraft, ipcMethod);
        } else {
            LOGGER.info("Not a relaunch, becoming controller!");

            IPCMethod ipcMethod;
            if (SocketUtil.isAfUnixSupported()) {
                ipcMethod = IPCMethod.Unix.inDirectory(Path.of(System.getProperty("user.home")));
            } else {
                int openPort = HttpUtil.getAvailablePort();
                ipcMethod = new IPCMethod.TCP(openPort);
            }

            bootstrapAsController(minecraft, ipcMethod);
        }

        SplitscreenSSClient.init();
    }

    private static void bootstrapAsPawn(Minecraft minecraft, IPCMethod connectionMethod) {
        remotePawnMain = new RemotePawnMain(minecraft, connectionMethod);
    }

    private static void bootstrapAsController(Minecraft minecraft, IPCMethod connectionMethod) {
        controller = new SplitscreenController(minecraft, connectionMethod, null);
    }

    public static boolean isSplitscreen() {
        return controller != null || remotePawnMain != null;
    }

    /**
     * Valid on both controller and pawn, empty if splitscreen is not active
     * @return the controller bridge
     */
    public static Optional<ControllerBridge> getControllerBridge() {
        return getController().<ControllerBridge>map(SplitscreenController::getControllerBridge)
                .or(() -> getPawn().<ControllerBridge>map(RemotePawnMain::getControllerBridge));
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
    public static Optional<RemotePawnMain> getPawn() {
        return Optional.ofNullable(remotePawnMain);
    }

    public static Optional<SplitscreenEngine> getEngine() {
        return getController().<SplitscreenEngine>map(SplitscreenController::getSplitscreenEngine)
                .or(() -> getPawn().<SplitscreenEngine>map(RemotePawnMain::getSplitscreenEngine));
    }

    /**
     * @return the side of the connection, either controller or pawn, empty if splitscreen is not active
     */
    public static Optional<Side> getSide() {
        if (controller != null) {
            return Optional.of(Side.CONTROLLER);
        } else if (remotePawnMain != null) {
            return Optional.of(Side.PAWN);
        } else {
            return Optional.empty();
        }
    }
}
