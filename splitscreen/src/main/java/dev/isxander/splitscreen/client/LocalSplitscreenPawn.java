package dev.isxander.splitscreen.client;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.features.configsync.ConfigSyncRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * A splitscreen pawn object that actually executes and controls a client.
 * Even the controller client is a pawn, and controls itself via this class' abstraction.
 */
public class LocalSplitscreenPawn implements SplitscreenPawn {
    private final Minecraft minecraft;

    private SplitscreenPosition position = null;
    private final int index;
    private final @Nullable ControllerUID associatedController;

    protected byte[] nonce;

    public LocalSplitscreenPawn(Minecraft minecraft, int index, @Nullable ControllerUID associatedController) {
        this.minecraft = minecraft;
        this.index = index;
        this.associatedController = associatedController;
    }

    @Override
    public int pawnIndex() {
        return this.index;
    }

    @Override
    public void joinServer(String host, int port, byte @Nullable [] nonce) {
        String ip = host + ":" + port;
        var address = new ServerAddress(host, port);
        var data = new ServerData("Splitscreen Master", ip, ServerData.Type.LAN);
        this.nonce = nonce;

        ConnectScreen.startConnecting(minecraft.screen, minecraft, address, data, false, null);
    }

    @Override
    public void disconnectFromServer() {
        PauseScreen.disconnectFromWorld(this.minecraft, ClientLevel.DEFAULT_QUIT_MESSAGE);
    }

    @Override
    public void closeGame() {
        this.minecraft.stop();
    }

    @Override
    public void useController(ControllerUID controllerUid) {
        Controlify.instance().setCurrentController(
                Controlify.instance().getControllerManager().orElseThrow()
                        .getConnectedControllers()
                        .stream().filter(controller -> controller.uid().equals(controllerUid))
                        .findAny().orElseThrow(),
                true
        );
    }

    @Override
    public void onConfigSave(ResourceLocation config) {
        ConfigSyncRegistry.onSave(config);
    }

    @Override
    public SplitscreenPosition getWindowSplitscreenMode() {
        return position;
    }

    @Override
    public @Nullable ControllerUID getAssociatedController() {
        return associatedController;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    public byte[] getLastLoginNonce() {
        return nonce;
    }

    public void setLastLoginNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}
