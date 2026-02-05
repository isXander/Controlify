package dev.isxander.splitscreen.client;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.features.configsync.ConfigSyncRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A splitscreen pawn object that actually executes and controls a client.
 * Even the controller client is a pawn, and controls itself via this class' abstraction.
 */
public class LocalSplitscreenPawn implements SplitscreenPawn {
    private final Minecraft minecraft;

    private final int index;
    private final InputMethod associatedInputMethod;

    protected byte @Nullable [] nonce;

    public LocalSplitscreenPawn(Minecraft minecraft, int index, InputMethod associatedInputMethod) {
        this.minecraft = minecraft;
        this.index = index;
        this.associatedInputMethod = associatedInputMethod;
    }

    @Override
    public int pawnIndex() {
        return this.index;
    }

    @Override
    public void joinServer(String host, int port, byte @Nullable [] nonce) {
        String ip = host + ":" + port;
        var address = new ServerAddress(host, port);
        var data = new ServerData("Splitscreen Host", ip, ServerData.Type.LAN);
        this.nonce = nonce;

        ConnectScreen.startConnecting(minecraft.screen, minecraft, address, data, false, null);
    }

    @Override
    public void disconnectFromServer() {
        this.minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
    }

    @Override
    public void closeGame() {
        this.minecraft.stop();
    }

    @Override
    public void useInputMethod(InputMethod inputMethod) {
        Controlify controlify = Controlify.instance();

        switch (inputMethod) {
            case InputMethod.KeyboardAndMouse() -> {
                controlify.setCurrentController(null, true);
            }
            case InputMethod.Controller(ControllerUID uid) -> {
                controlify.setCurrentController(
                        controlify.getControllerManager().orElseThrow()
                                .getConnectedControllers()
                                .stream().filter(c -> c.uid().equals(uid))
                                .findAny().orElseThrow(),
                        true
                );
            }
        }
    }

    @Override
    public void onConfigSave(Identifier config) {
        ConfigSyncRegistry.onSave(config);
    }

    @Override
    public InputMethod getAssociatedInputMethod() {
        return associatedInputMethod;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    public byte @Nullable [] getLastLoginNonce() {
        return nonce;
    }

    public void setLastLoginNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}
