package dev.isxander.splitscreen.engine.impl.fboshare.consumer;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.SplitscreenPosition;
import dev.isxander.splitscreen.engine.HostSplitscreenEngine;
import dev.isxander.splitscreen.engine.impl.fboshare.FboshareSplitscreenEngine;
import dev.isxander.splitscreen.engine.impl.fboshare.ipc.ControllerboundShareMemoryPayload;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FboshareHostSplitscreenEngine extends FboshareSplitscreenEngine implements HostSplitscreenEngine {


    @Override
    public void setSplitscreenMode(ControllerUID window, SplitscreenPosition position) {

    }

    @Override
    public void removeWindow(ControllerUID window) {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean consumeDirty() {
        return false;
    }

    @Override
    public boolean shouldExit() {
        return false;
    }

    private void handleShareMemory(ControllerUID window, CustomPacketPayload payload) {

    }

    @Override
    public void handleInboundPayload(ControllerUID window, Connection connection, CustomPacketPayload payload) {
        switch (payload) {
            case ControllerboundShareMemoryPayload shareMemoryPayload -> {
                // Handle the share memory payload
            }
            default -> throw new IllegalStateException("Unexpected value: " + payload);
        }
    }
}
