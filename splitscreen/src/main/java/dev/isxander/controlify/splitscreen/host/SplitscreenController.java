package dev.isxander.controlify.splitscreen.host;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.splitscreen.ipc.IPCMethod;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.host.ipc.ControllerConnectionListener;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import dev.isxander.controlify.splitscreen.window.ParentWindow;
import dev.isxander.controlify.splitscreen.window.ParentWindowEventHandler;
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.window.manager.WindowManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
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

    private final List<SplitscreenPawn> pawns = new ArrayList<>();
    private final ControllerConnectionListener connectionListener;
    private final LocalSplitscreenPawn localPawn;

    private final LocalControllerBridge controllerBridge;

    private @Nullable ParentWindow parentWindow;
    private boolean isWindowReady = false;
    private final Queue<Runnable> waitingForWindowTasks = new ArrayDeque<>();

    public SplitscreenController(Minecraft minecraft, IPCMethod connectionMethod) {
        this.minecraft = minecraft;
        this.controllerBridge = new LocalControllerBridge(minecraft, this);
        this.connectionListener = new ControllerConnectionListener(connectionMethod, this, minecraft);
        this.addPawn(this.localPawn = new HostLocalSplitscreenPawn(minecraft)); // control ourselves as a pawn

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
        int pawnIndex = this.pawns.size();
        LOGGER.info("Adding pawn #{}", pawnIndex);

        this.pawns.add(pawn);

        executeWhenWindowReady(parentWindow -> {
            ScreenSplitscreenMode splitscreenMode = ScreenSplitscreenBehaviour.getModeForScreen(this.minecraft.screen);

            pawn.setupWindowParent(
                    WindowManager.get().getNativeWindowHandle(parentWindow.getGlfwWindowHandle())
            );

            this.setSplitscreenMode(splitscreenMode);
        });

        ControlifyEvents.FINISHED_INIT.register(event -> {
            ControllerManager controllerManager = Controlify.instance().getControllerManager().orElseThrow();

            ControllerEntity controller = controllerManager.getConnectedControllers().get(pawnIndex);
            if (controller == null) {
                LOGGER.warn("Could not assign controller to pawn #{}: no usable controller found", pawnIndex);
                return;
            }

            pawn.useController(controller.uid());
        });
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
        switch (mode) {
            case FULLSCREEN -> {
                this.forEachPawn(pawn -> {
                    this.setPawnWindowSplitscreenMode(pawn, pawn == this.localPawn ?
                            SplitscreenPosition.FULL : SplitscreenPosition.HIDDEN);
                });
            }
            case SPLITSCREEN -> {
                int pawnCount = this.pawns.size();
                List<SplitscreenPosition> positions = switch (pawnCount) {
                    case 1 -> List.of(SplitscreenPosition.FULL);
                    case 2 -> List.of(SplitscreenPosition.LEFT, SplitscreenPosition.RIGHT);
                    case 3 -> List.of(SplitscreenPosition.LEFT, SplitscreenPosition.TOP_RIGHT, SplitscreenPosition.BOTTOM_RIGHT);
                    case 4 -> List.of(SplitscreenPosition.TOP_LEFT, SplitscreenPosition.TOP_RIGHT, SplitscreenPosition.BOTTOM_LEFT, SplitscreenPosition.BOTTOM_RIGHT);
                    default -> null;
                };
                if (positions == null) {
                    LOGGER.error("Splitscreen mode not supported for {} pawns", pawnCount);
                    return;
                }

                this.forEachPawn((pawn, i) -> {
                    SplitscreenPosition position = positions.get(i);
                    LOGGER.info("Setting pawn #{} to {}", i, position);
                    this.setPawnWindowSplitscreenMode(pawn, positions.get(i));
                });
            }
        }
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
        if (this.parentWindow != null) {
            consumer.accept(this.parentWindow);
        } else {
            this.waitingForWindowTasks.add(() -> consumer.accept(this.parentWindow));
        }
    }
}
