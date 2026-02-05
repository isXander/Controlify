package dev.isxander.splitscreen.client;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.Controlify;
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
 * Valid on both host and pawns, facilitates the abstraction and access
 * to the controller and pawn main classes.
 */
public class SplitscreenBootstrapper {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static @Nullable SplitscreenController controller;
    private static @Nullable RemotePawnMain remotePawnMain;

    /**
     * When a client is started, this method is called.
     * It initializes the pawn behaviour if the client is a pawn.
     */
    public static void bootstrap(Minecraft minecraft) {
        LOGGER.info("Boostrapping Controlify splitscreen!");

        ScreenSplitscreenModeRegistry.init();
        SplitscreenConfig.INSTANCE.loadFromFile();

        boolean relaunched = RelaunchArguments.RELAUNCHED.get().orElse(false);
        if (relaunched) {
            bootstrapAsPawn(minecraft);
        } else {
            LOGGER.info("Not a relaunch, deferring bootstrap to player action.");
        }

        SplitscreenSSClient.init();
    }

    public static void bootstrapAsPawn(Minecraft minecraft) {
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
    }

    private static void bootstrapAsPawn(Minecraft minecraft, IPCMethod connectionMethod) {
        remotePawnMain = new RemotePawnMain(minecraft, connectionMethod);
    }

    public static void boostrapAsController(Minecraft minecraft, InputMethod localInputMethod) {
        IPCMethod ipcMethod = IPCMethod.Unix.inDirectory(Path.of(System.getProperty("user.home")))
                .map(m -> (IPCMethod) m) // upcast the optional to IPCMethod instead of IPCMethod.Unix
                .filter(m -> SocketUtil.isAfUnixSupported())
                .orElseGet(() -> new IPCMethod.TCP(HttpUtil.getAvailablePort()));

        bootstrapAsController(minecraft, ipcMethod, localInputMethod);
    }

    private static void bootstrapAsController(Minecraft minecraft, IPCMethod connectionMethod, InputMethod localInputMethod) {
        controller = new SplitscreenController(minecraft, connectionMethod, localInputMethod);
        Controlify.instance().setCurrentController(
                localInputMethod.getControllerUID()
                        .flatMap(uid -> Controlify.instance().getControllerManager()
                                .flatMap(cm -> cm.getController(uid)))
                        .orElse(null),
                true
        );
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
     * @return the side of the connection, either controller or pawn
     */
    public static Side getSide() {
        boolean relaunched = RelaunchArguments.RELAUNCHED.get().orElse(false);
        return relaunched ? Side.PAWN : Side.CONTROLLER;
    }
}
