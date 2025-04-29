package dev.isxander.controlify.splitscreen.engine.impl.reparenting;

import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.engine.RemoteSplitscreenEngine;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ipc.ControllerboundThisIsMyWindowPayload;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ipc.PawnboundSetWindowActivePayload;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.WindowManager;
import dev.isxander.controlify.splitscreen.remote.RemoteControllerBridge;
import dev.isxander.controlify.splitscreen.remote.RemotePawnMain;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public class ReparentingRemoteSplitscreenEngine extends ReparentingSplitscreenEngine implements RemoteSplitscreenEngine {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;
    private final WindowManager windowManager;

    private final RemoteControllerBridge bridge;
    private final LocalSplitscreenPawn localMainPawn;
    private @Nullable LocalReparentingPawn localPawn;

    public ReparentingRemoteSplitscreenEngine(Minecraft minecraft, RemoteControllerBridge bridge, LocalSplitscreenPawn localMainPawn) {
        this.minecraft = minecraft;
        this.windowManager = WindowManager.get();
        this.bridge = bridge;
        this.localMainPawn = localMainPawn;
    }

    public static Optional<ReparentingRemoteSplitscreenEngine> tryGet(RemotePawnMain remotePawnMain) {
        if (remotePawnMain.getSplitscreenEngine() instanceof ReparentingRemoteSplitscreenEngine engine) {
            return Optional.of(engine);
        }
        return Optional.empty();
    }

    public void onWindowInit() {
        Window window = this.minecraft.getWindow();
        NativeWindowHandle nativeHandle = this.windowManager.getNativeWindowHandle(window.getWindow());

        this.bridge.sendEnginePayload(new ControllerboundThisIsMyWindowPayload(nativeHandle));
    }

    public void onPayloadSetWindowActive(@NotNull LocalReparentingPawn localPawn, PawnboundSetWindowActivePayload payload) {
        localPawn.setWindowFocusState(payload.active());
    }

    @Override
    public void handleInboundPayload(CustomPacketPayload payload) {
        if (this.localPawn == null) throw new IllegalStateException("Local pawn not initialized");

        switch (payload) {
            case PawnboundSetWindowActivePayload p -> this.onPayloadSetWindowActive(this.localPawn, p);
            default -> LOGGER.error("Unknown payload type: {}", payload.getClass().getName());
        }
    }
}
