package dev.isxander.splitscreen.client.host;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.host.features.music.PawnMusicManager;
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

    private final PawnMusicManager pawnMusicManager;

    private final Map<InputMethod, RelaunchProcessHandler> relaunchProcessHandlers = new HashMap<>();
    private final Map<InputMethod, PendingRelaunchClientStatus> pendingRelaunchClients = new HashMap<>();
    private @Nullable SplitscreenFakeReloadInstance splitscreenLoaderStatus = null;

    public SplitscreenController(Minecraft minecraft, IPCMethod ipcMethod, InputMethod associatedInputMethod) {
        this.minecraft = minecraft;
        this.controllerBridge = new LocalControllerBridge(minecraft, this);
        this.ipcMethod = ipcMethod;
        this.connectionListener = new ControllerConnectionListener(ipcMethod, this, minecraft);
        this.addPawn(this.localPawn = new HostLocalSplitscreenPawn(minecraft, associatedInputMethod)); // control ourselves as a pawn
        this.splitscreenEngine = HostSplitscreenEngine.create(this.minecraft, associatedInputMethod);
        this.pawnMusicManager = new PawnMusicManager();

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

    public List<SplitscreenPawn> getPawns() {
        return Collections.unmodifiableList(this.pawns);
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

        if (pawn instanceof RemoteSplitscreenPawn) {
            InputMethod associatedController = pawn.getAssociatedInputMethod();

            PendingRelaunchClientStatus newStatus = new PendingRelaunchClientStatus.WaitingForReadySignal(0);
            PendingRelaunchClientStatus oldStatus = this.pendingRelaunchClients.put(associatedController, newStatus);

            if (!(oldStatus instanceof PendingRelaunchClientStatus.WaitingForConnection)) {
                LOGGER.warn("Pawn connected with controller {} but we were not expecting it", associatedController);
            }
        }
    }

    public void onPawnReadySignal(boolean finished, float progress, RemoteSplitscreenPawn pawn, InputMethod inputMethod) {
        RelaunchProcessHandler process = this.relaunchProcessHandlers.get(inputMethod);
        process.setPawn(pawn);

        if (finished) {
            this.pendingRelaunchClients.remove(inputMethod);
            LOGGER.info("Pawn {} is ready", inputMethod);

            this.updateSplitscreenMode();
        } else {
            var newStatus = new PendingRelaunchClientStatus.WaitingForReadySignal(progress);
            var oldStatus = this.pendingRelaunchClients.put(inputMethod, newStatus);

            if (oldStatus == null) {
                LOGGER.warn("Pawn {} sent ready update but we were not waiting on it", inputMethod);
            }
        }
    }

    public void removePawn(SplitscreenPawn pawn) {
        this.pawns.remove(pawn);
        this.splitscreenEngine.removeWindow(pawn.getAssociatedInputMethod());
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
                            pawn.getAssociatedInputMethod(),
                            pawn == this.localPawn ? SplitscreenPosition.FULL : SplitscreenPosition.HIDDEN
                    );
                });
            }
            case SPLITSCREEN -> {
                int pawnCount = this.pawns.size();
                boolean horizontal = !SplitscreenConfig.INSTANCE.preferVerticalSplitscreen.get();

                SplitscreenPosition.Visible[] positions = SplitscreenPosition.Visible.arrangeForN(pawnCount, horizontal);

                this.forEachPawn((pawn, i) -> {
                    SplitscreenPosition position = positions[i];
                    LOGGER.info("Setting pawn #{} to {}", i, position);
                    this.splitscreenEngine.setSplitscreenMode(pawn.getAssociatedInputMethod(), position);
                });
            }
        }
    }

    public void updateSplitscreenMode() {
        this.setSplitscreenMode(ScreenSplitscreenModeRegistry.getMode(this.minecraft.screen));
    }

    public PawnMusicManager getPawnMusicManager() {
        return this.pawnMusicManager;
    }

    /**
     * Relaunches the game to add another player, bound to a specific controller.
     *
     * @param inputMethod the input method to associate with this new pawn
     * @return if the pawn was successfully summoned
     */
    public boolean summonNewPawnClient(InputMethod inputMethod) {
        if (this.relaunchProcessHandlers.containsKey(inputMethod)) {
            return false;
        }

        int pawnIndex = this.pawns.size();
        RelaunchProcessHandler handler = RelaunchProcessHandler.createProcess(this.minecraft, inputMethod, this, pawnIndex, this.ipcMethod);
        this.relaunchProcessHandlers.put(inputMethod, handler);
        this.pendingRelaunchClients.put(inputMethod, new PendingRelaunchClientStatus.WaitingForConnection());

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
                this.onRelaunchedPawnExit(inputMethod, handler, throwable));

        return true;
    }

    private void onRelaunchedPawnExit(InputMethod inputMethod, RelaunchProcessHandler process, Throwable throwable) {
        if (this.pendingRelaunchClients.remove(inputMethod) != null) {
            LOGGER.info("Relaunch client exited before it was ready, did it crash?");
        }

        if (this.relaunchProcessHandlers.remove(inputMethod) != null) {
            LOGGER.info("Relaunch process for {} exited", inputMethod, throwable);
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
