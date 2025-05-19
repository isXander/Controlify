package dev.isxander.controlify.splitscreen.engine.impl.reparenting;

import dev.isxander.controlify.splitscreen.engine.SplitscreenEnginePayloadSender;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ipc.PawnboundSetWindowActivePayload;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ipc.PawnboundThrottleFrameratePayload;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;

public class RemoteReparentingPawn implements ReparentingPawn {

    private final NativeWindowHandle windowHandle;
    private final SplitscreenEnginePayloadSender sender;

    public RemoteReparentingPawn(SplitscreenEnginePayloadSender sender, NativeWindowHandle windowHandle) {
        this.sender = sender;
        this.windowHandle = windowHandle;
    }

    @Override
    public void setWindowFocusState(boolean active) {
        this.sender.sendPayload(new PawnboundSetWindowActivePayload(active));
    }

    @Override
    public void setThrottleFramerate(boolean throttle) {
        this.sender.sendPayload(new PawnboundThrottleFrameratePayload(throttle));
    }

    @Override
    public NativeWindowHandle getNativeWindowHandle() {
        return this.windowHandle;
    }
}
