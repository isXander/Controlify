package dev.isxander.splitscreen.client.remote;

import dev.isxander.splitscreen.client.LocalSplitscreenPawn;
import dev.isxander.splitscreen.client.engine.RemoteSplitscreenEngine;
import dev.isxander.splitscreen.client.ipc.IPCMethod;
import dev.isxander.splitscreen.client.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchArguments;
import dev.isxander.splitscreen.client.remote.ipc.PawnConnectionListener;
import net.minecraft.client.Minecraft;

public class RemotePawnMain {

    private final Minecraft minecraft;
    private final LocalSplitscreenPawn pawn;
    private final RemoteControllerBridge controllerBridge;
    private final PawnConnectionListener connectionListener;
    private final RemoteSplitscreenEngine splitscreenEngine;

    public RemotePawnMain(Minecraft minecraft, IPCMethod ipcMethod) {
        this.minecraft = minecraft;
        this.pawn = new LocalSplitscreenPawn(minecraft, RelaunchArguments.PAWN_INDEX.get().orElseThrow(), RelaunchArguments.CONTROLLER.get().orElse(null));
        this.splitscreenEngine = RemoteSplitscreenEngine.create(this.minecraft, payload -> this.getConnectionListener().getControllerConnection().send(new ControllerboundEngineCustomPayloadPacket(payload)), this.pawn);
        this.connectionListener = new PawnConnectionListener(this.minecraft, ipcMethod, this);
        this.controllerBridge = new RemoteControllerBridge(this.minecraft, this.connectionListener.getControllerConnection());

        PawnScreenOverrides.init();
    }

    public LocalSplitscreenPawn getPawn() {
        return this.pawn;
    }

    public PawnConnectionListener getConnectionListener() {
        return this.connectionListener;
    }

    public RemoteControllerBridge getControllerBridge() {
        return this.controllerBridge;
    }

    public RemoteSplitscreenEngine getSplitscreenEngine() {
        return this.splitscreenEngine;
    }
}
