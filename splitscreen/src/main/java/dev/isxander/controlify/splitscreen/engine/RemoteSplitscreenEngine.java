package dev.isxander.controlify.splitscreen.engine;

import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ReparentingRemoteSplitscreenEngine;
import dev.isxander.controlify.splitscreen.remote.RemoteControllerBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface RemoteSplitscreenEngine extends SplitscreenEngine {

    static RemoteSplitscreenEngine create(Minecraft minecraft, RemoteControllerBridge bridge, LocalSplitscreenPawn pawn) {
        return new ReparentingRemoteSplitscreenEngine(minecraft, bridge, pawn);
    }

    void handleInboundPayload(CustomPacketPayload payload);
}
