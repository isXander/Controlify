package dev.isxander.controlify.splitscreen.host;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.host.gui.SplitscreenFakeReloadInstance;
import dev.isxander.controlify.splitscreen.host.gui.SplitscreenLoadingOverlay;
import dev.isxander.controlify.splitscreen.ipc.IPCMethod;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.host.ipc.ControllerConnectionListener;
import dev.isxander.controlify.splitscreen.host.relaunch.RelaunchProcessHandler;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import dev.isxander.controlify.splitscreen.window.ParentWindow;
import dev.isxander.controlify.splitscreen.window.ParentWindowEventHandler;
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.window.manager.NativeWindowHandle;
import dev.isxander.controlify.splitscreen.window.manager.WindowManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The main class for the controller side of the splitscreen mod.
 * This class facilitates communication with pawns, as well as holding the
 * parent window that all pawns attach to.
 */
public class SplitscreenController implements ParentWindowEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;

    private final ControllerConnectionListener connectionListener;
    private final IPCMethod ipcMethod;

    private final List<SplitscreenPawn> pawns = new ArrayList<>();
    private final LocalSplitscreenPawn localPawn;

    private final LocalControllerBridge controllerBridge;

    private @Nullable ParentWindow parentWindow;
    private boolean isWindowReady = false;
    private final Queue<Runnable> waitingForWindowTasks = new ArrayDeque<>();

    private final Map<ControllerUID, RelaunchProcessHandler> relaunchProcessHandlers = new HashMap<>();
    private final Map<ControllerUID, PendingRelaunchClientStatus> pendingRelaunchClients = new HashMap<>();
    private @Nullable SplitscreenFakeReloadInstance splitscreenLoaderStatus = null;

    public SplitscreenController(Minecraft minecraft, IPCMethod ipcMethod, @Nullable ControllerUID associatedController) {
        this.minecraft = minecraft;
        this.controllerBridge = new LocalControllerBridge(minecraft, this);
        this.ipcMethod = ipcMethod;
        this.connectionListener = new ControllerConnectionListener(ipcMethod, this, minecraft);
        this.addPawn(this.localPawn = new HostLocalSplitscreenPawn(minecraft, associatedController)); // control ourselves as a pawn

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            this.connectionListener.tick();
        });
    }

    public void forEachPawn(Consumer<SplitscreenPawn> consumer) {
        pawns.forEach(consumer);
    }

    public void forEachPawn(BiConsumer<SplitscreenPawn, Integer> consumer) {
        for (int i = 0; i < pawns.size(); i++) {
            consumer.accept(pawns.get(i), i);
        }
    }

    public void addPawn(SplitscreenPawn pawn) {
        // TODO: receive the pawn index from the pawn itself as there may be a mismatch with relaunch (race condition)
        int pawnIndex = this.pawns.size();
        LOGGER.info("Adding pawn #{}", pawnIndex);

        this.pawns.add(pawn);

        executeWhenWindowReady(parentWindow -> {
            pawn.setupWindowParent(
                    WindowManager.get().getNativeWindowHandle(parentWindow.getGlfwWindowHandle())
            );

            this.setPawnWindowSplitscreenMode(pawn, SplitscreenPosition.HIDDEN);
        });

        @Nullable ControllerUID associatedController = pawn.getAssociatedController();
        if (associatedController != null) {
            PendingRelaunchClientStatus newStatus = new PendingRelaunchClientStatus.WaitingForReadySignal(0);
            PendingRelaunchClientStatus oldStatus = this.pendingRelaunchClients.put(associatedController, newStatus);

            if (!(oldStatus instanceof PendingRelaunchClientStatus.WaitingForConnection)) {
                LOGGER.warn("Pawn connected with controller {} but we were not expecting it", associatedController);
            }
        }
    }

    public void onPawnReadySignal(boolean finished, float progress, @Nullable ControllerUID controllerUid) {
        if (controllerUid == null) {
            LOGGER.warn("Pawn ready signal received with no controller UID");
            return;
        }

        if (finished) {
            this.pendingRelaunchClients.remove(controllerUid);
            LOGGER.info("Pawn {} is ready", controllerUid);

            this.updateSplitscreenMode();

            NativeWindowHandle parentHandle = WindowManager.get().getNativeWindowHandle(this.parentWindow.getGlfwWindowHandle());
            NativeWindowHandle childHandle = WindowManager.get().getNativeWindowHandle(this.minecraft.getWindow().getWindow());
            WindowManager.get().setWindowForeground(parentHandle);
            WindowManager.get().setWindowFocused(childHandle);

            this.forEachPawn(pawn -> {
                pawn.setWindowFocusState(true);
            });
        } else {
            var newStatus = new PendingRelaunchClientStatus.WaitingForReadySignal(progress);
            var oldStatus = this.pendingRelaunchClients.put(controllerUid, newStatus);

            if (oldStatus == null) {
                LOGGER.warn("Pawn {} sent ready update but we were not waiting on it", controllerUid);
            }
        }
    }

    public void removePawn(SplitscreenPawn pawn) {
        this.pawns.remove(pawn);
    }

    public int getPawnCount() {
        return this.pawns.size();
    }

    public LocalSplitscreenPawn getLocalPawn() {
        return this.localPawn;
    }

    public LocalControllerBridge getControllerBridge() {
        return this.controllerBridge;
    }

    public @Nullable ParentWindow getParentWindow() {
        return this.parentWindow;
    }

    public void setupParentWindow(DisplayData screenSize, ScreenManager screenManager, String initialTitle) {
        RenderSystem.assertOnRenderThread();

        LOGGER.info("Setting up parent window");

        if (this.parentWindow != null) {
            LOGGER.warn("Parent window already set up, skipping");
            return;
        }

        this.parentWindow = new ParentWindow(this.minecraft, screenSize, screenManager, this, initialTitle);

        executeWhenWindowReady(parentWindow -> {
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                if (this.parentWindow.shouldClose() && this.minecraft.isRunning()) {
                    this.forEachPawn(SplitscreenPawn::closeGame);
                }
            });
        });

    }

    @Override
    public void onResizeParentWindow(int width, int height) {
        this.forEachPawn(pawn -> pawn.setWindowSplitscreenMode(pawn.getWindowSplitscreenMode(), width, height));
    }

    @Override
    public void onFocusParentWindow(boolean focused) {

    }

    public void setSplitscreenMode(ScreenSplitscreenMode mode) {
        // Do not allow splitscreen if we're loading something, it will ruin the illusion.
        if (this.minecraft.getOverlay() != null) {
            mode = ScreenSplitscreenMode.FULLSCREEN;
        }

        switch (mode) {
            case FULLSCREEN -> {
                this.forEachPawn(pawn -> {
                    this.setPawnWindowSplitscreenMode(pawn, pawn == this.localPawn ?
                            SplitscreenPosition.FULL : SplitscreenPosition.HIDDEN);
                });
            }
            case SPLITSCREEN -> {
                int pawnCount = this.pawns.size();
                SplitscreenPosition.Visible[] positions = switch (pawnCount) {
                    case 1 -> new SplitscreenPosition.Visible[]{SplitscreenPosition.FULL};
                    case 2 -> SplitscreenPosition.LEFT_RIGHT;
                    case 3 -> SplitscreenPosition.LEFT_TOP_BOTTOM;
                    case 4 -> SplitscreenPosition.FOUR_WAY;
                    default -> SplitscreenPosition.Visible.arrangeInGridForN(pawnCount);
                };

                this.forEachPawn((pawn, i) -> {
                    SplitscreenPosition position = positions[i];
                    LOGGER.info("Setting pawn #{} to {}", i, position);
                    this.setPawnWindowSplitscreenMode(pawn, position);
                });
            }
        }
    }

    public void updateSplitscreenMode() {
        this.setSplitscreenMode(PawnSplitscreenModeRegistry.getMode(this.minecraft.screen));
    }

    public void markWindowReady() {
        if (this.isWindowReady) return;
        if (this.parentWindow == null) throw new IllegalStateException("markWindowReady called before the ParentWindow has been created.");
        // noinspection ConstantConditions
        if (this.minecraft.getWindow() == null) throw new IllegalStateException("markWindowReady called before the vanilla Window has been created.");
        this.isWindowReady = true;

        while (!this.waitingForWindowTasks.isEmpty()) {
            this.waitingForWindowTasks.poll().run();
        }
    }

    private void setPawnWindowSplitscreenMode(SplitscreenPawn pawn, SplitscreenPosition pos) {
        int width = this.parentWindow.getWidth();
        int height = this.parentWindow.getHeight();

        pawn.setWindowSplitscreenMode(pos, width, height);
    }

    private void executeWhenWindowReady(Consumer<@NotNull ParentWindow> consumer) {
        if (this.isWindowReady) {
            consumer.accept(this.parentWindow);
        } else {
            this.waitingForWindowTasks.add(() -> consumer.accept(this.parentWindow));
        }
    }

    /**
     * Relaunches the game to add another player, bound to a specific controller.
     *
     * @param controller the controller to associate with this new pawn
     * @return if the pawn was successfully summoned
     */
    public boolean summonNewPawnClient(ControllerUID controller) {
        if (this.relaunchProcessHandlers.containsKey(controller)) {
            return false;
        }

        int pawnIndex = this.pawns.size();
        RelaunchProcessHandler handler = RelaunchProcessHandler.createProcess(this.minecraft, controller, pawnIndex, this.ipcMethod);
        this.relaunchProcessHandlers.put(controller, handler);
        this.pendingRelaunchClients.put(controller, new PendingRelaunchClientStatus.WaitingForConnection());

        if (this.splitscreenLoaderStatus == null) {
            this.splitscreenLoaderStatus = new SplitscreenFakeReloadInstance(this.pendingRelaunchClients.values());

            if (this.minecraft.getOverlay() != null) {
                LOGGER.error("Tried to open the splitscreen loading overlay but another overlay was open");
            } else {
                this.minecraft.setOverlay(new SplitscreenLoadingOverlay(this.minecraft, this.splitscreenLoaderStatus, (err) -> {
                    this.splitscreenLoaderStatus = null;
                }, true));
            }
        }

        handler.onExit().whenComplete((h, throwable) -> {
            if (this.pendingRelaunchClients.remove(controller) != null) {
                LOGGER.info("Relaunch client exited before it was ready, did it crash?");
            }
        });

        return true;
    }
}
