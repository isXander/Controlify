package dev.isxander.controlify.splitscreen.engine;

import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ReparentingRemoteSplitscreenEngine;
import dev.isxander.controlify.splitscreen.remote.RemoteControllerBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface RemoteSplitscreenEngine extends SplitscreenEngine {

    static RemoteSplitscreenEngine create(Minecraft minecraft, SplitscreenEnginePayloadSender payloadSender, LocalSplitscreenPawn pawn) {
        return new ReparentingRemoteSplitscreenEngine(minecraft, payloadSender, pawn);
    }

    void handleInboundPayload(CustomPacketPayload payload);
}
