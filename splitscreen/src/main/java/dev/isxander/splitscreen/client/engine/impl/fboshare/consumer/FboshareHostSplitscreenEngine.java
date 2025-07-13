package dev.isxander.splitscreen.client.engine.impl.fboshare.consumer;

import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.SplitscreenPosition;
import dev.isxander.splitscreen.client.engine.HostSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.fboshare.FboshareSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.fboshare.ipc.ControllerboundShareMemoryPayload;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FboshareHostSplitscreenEngine extends FboshareSplitscreenEngine implements HostSplitscreenEngine {


    @Override
    public void setSplitscreenMode(InputMethod window, SplitscreenPosition position) {

    }

    @Override
    public void removeWindow(InputMethod window) {

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

    private void handleShareMemory(InputMethod window, CustomPacketPayload payload) {

    }

    @Override
    public void handleInboundPayload(InputMethod window, Connection connection, CustomPacketPayload payload) {
        switch (payload) {
            case ControllerboundShareMemoryPayload shareMemoryPayload -> {
                // Handle the share memory payload
            }
            default -> throw new IllegalStateException("Unexpected value: " + payload);
        }
    }
}
