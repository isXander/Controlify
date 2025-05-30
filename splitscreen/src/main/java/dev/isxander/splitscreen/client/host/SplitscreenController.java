package dev.isxander.splitscreen.client.host;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.host.features.relaunch.PendingRelaunchClientStatus;
import dev.isxander.splitscreen.client.config.SplitscreenConfig;
import dev.isxander.splitscreen.client.engine.HostSplitscreenEngine;
import dev.isxander.splitscreen.client.host.gui.SplitscreenFakeReloadInstance;
import dev.isxander.splitscreen.client.host.gui.SplitscreenLoadingOverlay;
import dev.isxander.splitscreen.client.ipc.IPCMethod;
import dev.isxander.splitscreen.client.SplitscreenPawn;
import dev.isxander.splitscreen.client.host.ipc.ControllerConnectionListener;
import dev.isxander.splitscreen.client.host.features.relaunch.RelaunchProcessHandler;
import dev.isxander.splitscreen.client.features.screenop.ScreenSplitscreenModeRegistry;
import dev.isxander.splitscreen.client.features.screenop.ScreenSplitscreenMode;
import dev.isxander.splitscreen.client.SplitscreenPosition;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
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
public class SplitscreenController  {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;

    private final ControllerConnectionListener connectionListener;
    private final IPCMethod ipcMethod;

    private final List<SplitscreenPawn> pawns = new ArrayList<>();
    private final HostLocalSplitscreenPawn localPawn;

    private final LocalControllerBridge controllerBridge;

    private final HostSplitscreenEngine splitscreenEngine;

    private final Map<ControllerUID, RelaunchProcessHandler> relaunchProcessHandlers = new HashMap<>();
    private final Map<ControllerUID, PendingRelaunchClientStatus> pendingRelaunchClients = new HashMap<>();
    private @Nullable SplitscreenFakeReloadInstance splitscreenLoaderStatus = null;

    public SplitscreenController(Minecraft minecraft, IPCMethod ipcMethod, @Nullable ControllerUID associatedController) {
        this.minecraft = minecraft;
        this.controllerBridge = new LocalControllerBridge(minecraft, this);
        this.ipcMethod = ipcMethod;
        this.connectionListener = new ControllerConnectionListener(ipcMethod, this, minecraft);
        this.addPawn(this.localPawn = new HostLocalSplitscreenPawn(minecraft, associatedController)); // control ourselves as a pawn
        this.splitscreenEngine = HostSplitscreenEngine.create(this.minecraft, associatedController);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            this.connectionListener.tick();

            if (this.splitscreenEngine.consumeDirty()) {
                this.updateSplitscreenMode();
            }

            if (this.splitscreenEngine.shouldExit()) {
                this.minecraft.stop();
            }
        });
    }

    public void forEachPawn(Consumer<SplitscreenPawn> consumer) {
        this.pawns.forEach(consumer);
    }

    public void forEachPawn(BiConsumer<SplitscreenPawn, Integer> consumer) {
        for (int i = 0; i < pawns.size(); i++) {
            consumer.accept(pawns.get(i), i);
        }
    }

    public int getNextPawnIndex() {
        return this.pawns.size();
    }

    public void addPawn(SplitscreenPawn pawn) {
        int pawnIndex = this.getNextPawnIndex();

        if (pawn.pawnIndex() != pawnIndex) {
            throw new IllegalArgumentException("pawn's index does not match pawn list size. race condition?");
        }

        LOGGER.info("Adding pawn #{}", pawnIndex);

        this.pawns.add(pawn);

        @Nullable ControllerUID associatedController = pawn.getAssociatedController();
        if (associatedController != null) {
            PendingRelaunchClientStatus newStatus = new PendingRelaunchClientStatus.WaitingForReadySignal(0);
            PendingRelaunchClientStatus oldStatus = this.pendingRelaunchClients.put(associatedController, newStatus);

            if (!(oldStatus instanceof PendingRelaunchClientStatus.WaitingForConnection)) {
                LOGGER.warn("Pawn connected with controller {} but we were not expecting it", associatedController);
            }
        }
    }

    public void onPawnReadySignal(boolean finished, float progress, RemoteSplitscreenPawn pawn, @Nullable ControllerUID controllerUid) {
        if (controllerUid == null) {
            LOGGER.warn("Pawn ready signal received with no controller UID");
            return;
        }

        RelaunchProcessHandler process = this.relaunchProcessHandlers.get(controllerUid);
        process.setPawn(pawn);

        if (finished) {
            this.pendingRelaunchClients.remove(controllerUid);
            LOGGER.info("Pawn {} is ready", controllerUid);

            this.updateSplitscreenMode();
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
        this.splitscreenEngine.removeWindow(pawn.getAssociatedController());
        this.splitscreenEngine.consumeDirty();
        this.updateSplitscreenMode();
    }

    public int getPawnCount(boolean includeLocal) {
        return this.pawns.size() - (includeLocal ? 0 : 1);
    }

    public HostLocalSplitscreenPawn getLocalPawn() {
        return this.localPawn;
    }

    public LocalControllerBridge getControllerBridge() {
        return this.controllerBridge;
    }

    public void setSplitscreenMode(ScreenSplitscreenMode mode) {
        // Do not allow splitscreen if we're loading something, it will ruin the illusion.
        if (this.minecraft.getOverlay() != null) {
            mode = ScreenSplitscreenMode.FULLSCREEN;
        }

        switch (mode) {
            case FULLSCREEN -> {
                this.forEachPawn(pawn -> {
                    this.splitscreenEngine.setSplitscreenMode(
                            pawn.getAssociatedController(),
                            pawn == this.localPawn ? SplitscreenPosition.FULL : SplitscreenPosition.HIDDEN
                    );
                });
            }
            case SPLITSCREEN -> {
                int pawnCount = this.pawns.size();
                boolean horizontal = !SplitscreenConfig.INSTANCE.preferVerticalSplitscreen.get();

                SplitscreenPosition.Visible[] positions = switch (pawnCount) {
                    case 1 -> new SplitscreenPosition.Visible[]{SplitscreenPosition.FULL};
                    case 2 -> horizontal ? SplitscreenPosition.LEFT_RIGHT : SplitscreenPosition.TOP_BOTTOM;
                    case 3 -> horizontal ? SplitscreenPosition.LEFT_TOP_BOTTOM : SplitscreenPosition.LEFT_RIGHT_BOTTOM;
                    case 4 -> SplitscreenPosition.FOUR_WAY;
                    default -> SplitscreenPosition.Visible.arrangeInGridForN(pawnCount);
                };

                this.forEachPawn((pawn, i) -> {
                    SplitscreenPosition position = positions[i];
                    LOGGER.info("Setting pawn #{} to {}", i, position);
                    this.splitscreenEngine.setSplitscreenMode(pawn.getAssociatedController(), position);
                });
            }
        }
    }

    public void updateSplitscreenMode() {
        this.setSplitscreenMode(ScreenSplitscreenModeRegistry.getMode(this.minecraft.screen));
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
        RelaunchProcessHandler handler = RelaunchProcessHandler.createProcess(this.minecraft, controller, this, pawnIndex, this.ipcMethod);
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

        handler.onExit().whenComplete((h, throwable) ->
                this.onRelaunchedPawnExit(controller, handler, throwable));

        return true;
    }

    private void onRelaunchedPawnExit(ControllerUID controller, RelaunchProcessHandler process, Throwable throwable) {
        if (this.pendingRelaunchClients.remove(controller) != null) {
            LOGGER.info("Relaunch client exited before it was ready, did it crash?");
        }

        if (this.relaunchProcessHandlers.remove(controller) != null) {
            LOGGER.info("Relaunch process for {} exited", controller);
        }

        SplitscreenPawn pawn = process.getPawn();
        if (pawn != null) {
            this.removePawn(pawn);
        }
    }

    public HostSplitscreenEngine getSplitscreenEngine() {
        return this.splitscreenEngine;
    }
}
