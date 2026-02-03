package dev.isxander.splitscreen.client.engine.impl.reparenting;

import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import dev.isxander.splitscreen.client.LocalSplitscreenPawn;
import dev.isxander.splitscreen.client.engine.RemoteSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.SplitscreenEnginePayloadSender;
import dev.isxander.splitscreen.client.engine.impl.reparenting.events.VanillaWindowFocusEvent;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ipc.ControllerboundTakeFocusPayload;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ipc.ControllerboundThisIsMyWindowPayload;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ipc.PawnboundSetWindowActivePayload;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ipc.PawnboundThrottleFrameratePayload;
import dev.isxander.splitscreen.client.engine.impl.reparenting.wm.NativeWindowHandle;
import dev.isxander.splitscreen.client.engine.impl.reparenting.wm.WindowManager;
import dev.isxander.splitscreen.client.remote.RemotePawnMain;
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

    private final SplitscreenEnginePayloadSender payloadSender;
    private final LocalSplitscreenPawn localMainPawn;
    private @Nullable LocalReparentingPawn localPawn;

    public ReparentingRemoteSplitscreenEngine(Minecraft minecraft, SplitscreenEnginePayloadSender payloadSender, LocalSplitscreenPawn localMainPawn) {
        this.minecraft = minecraft;
        this.windowManager = WindowManager.get();
        this.payloadSender = payloadSender;
        this.localMainPawn = localMainPawn;

        VanillaWindowFocusEvent.EVENT.register((window, focused) -> {
            if (focused) giveFocusToController();
        });
    }

    public boolean shouldThrottleFps() {
        return localPawn != null && localPawn.shouldThrottleFramerate();
    }

    public static Optional<ReparentingRemoteSplitscreenEngine> tryGet(RemotePawnMain remotePawnMain) {
        if (remotePawnMain.getSplitscreenEngine() instanceof ReparentingRemoteSplitscreenEngine engine) {
            return Optional.of(engine);
        }
        return Optional.empty();
    }

    public void onWindowInit() {
        Window window = this.minecraft.getWindow();
        NativeWindowHandle nativeHandle = this.windowManager.getNativeWindowHandle(window.handle());

        this.localPawn = new LocalReparentingPawn(this.minecraft, nativeHandle);
        this.payloadSender.sendPayload(new ControllerboundThisIsMyWindowPayload(nativeHandle));
    }

    @Override
    public void handleInboundPayload(CustomPacketPayload payload) {
        if (this.localPawn == null) throw new IllegalStateException("Local pawn not initialized");

        switch (payload) {
            case PawnboundSetWindowActivePayload p -> this.onPayloadSetWindowActive(this.localPawn, p);
            case PawnboundThrottleFrameratePayload p -> this.onThrottleFramerate(this.localPawn, p);
            default -> LOGGER.error("Unknown payload type: {}", payload.getClass().getName());
        }
    }

    private void giveFocusToController() {
        this.payloadSender.sendPayload(new ControllerboundTakeFocusPayload());
        this.localPawn.setWindowFocusState(true);
    }

    private void onPayloadSetWindowActive(@NotNull LocalReparentingPawn localPawn, PawnboundSetWindowActivePayload payload) {
        localPawn.setWindowFocusState(payload.active());
    }

    private void onThrottleFramerate(@NotNull LocalReparentingPawn localPawn, PawnboundThrottleFrameratePayload payload) {
        localPawn.setThrottleFramerate(payload.throttle());
    }
}
