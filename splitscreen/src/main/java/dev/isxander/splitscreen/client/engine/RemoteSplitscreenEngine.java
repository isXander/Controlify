package dev.isxander.splitscreen.client.engine;

import dev.isxander.splitscreen.client.LocalSplitscreenPawn;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ReparentingRemoteSplitscreenEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface RemoteSplitscreenEngine extends SplitscreenEngine {

    static RemoteSplitscreenEngine create(Minecraft minecraft, SplitscreenEnginePayloadSender payloadSender, LocalSplitscreenPawn pawn) {
        return new ReparentingRemoteSplitscreenEngine(minecraft, payloadSender, pawn);
    }

    void handleInboundPayload(CustomPacketPayload payload);
}
