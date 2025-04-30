package dev.isxander.controlify.splitscreen.remote;

import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.engine.RemoteSplitscreenEngine;
import dev.isxander.controlify.splitscreen.ipc.IPCMethod;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.controlify.splitscreen.relauncher.RelaunchArguments;
import dev.isxander.controlify.splitscreen.remote.ipc.PawnConnectionListener;
import dev.isxander.controlify.splitscreen.screenop.PawnSplitscreenModeRegistry;
import net.minecraft.client.Minecraft;

public class RemotePawnMain {

    private final Minecraft minecraft;
    private final LocalSplitscreenPawn pawn;
    private final RemoteControllerBridge controllerBridge;
    private final PawnConnectionListener connectionListener;
    private final RemoteSplitscreenEngine splitscreenEngine;

    public RemotePawnMain(Minecraft minecraft, IPCMethod ipcMethod) {
        this.minecraft = minecraft;
        this.pawn = new LocalSplitscreenPawn(minecraft, RelaunchArguments.CONTROLLER.get().orElse(null));
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
