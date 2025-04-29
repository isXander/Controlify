package dev.isxander.controlify.splitscreen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.jetbrains.annotations.Nullable;

/**
 * A splitscreen pawn object that actually executes and controls a client.
 * Even the controller client is a pawn, and controls itself via this class' abstraction.
 */
public class LocalSplitscreenPawn implements SplitscreenPawn {
    private final Minecraft minecraft;

    private SplitscreenPosition position = null;
    private final @Nullable ControllerUID associatedController;

    public LocalSplitscreenPawn(Minecraft minecraft, @Nullable ControllerUID associatedController) {
        this.minecraft = minecraft;
        this.associatedController = associatedController;
    }

    @Override
    public void joinServer(String host, int port) {
        String ip = host + ":" + port;
        var address = new ServerAddress(host, port);
        var data = new ServerData("Splitscreen Master", ip, ServerData.Type.LAN);

        ConnectScreen.startConnecting(minecraft.screen, minecraft, address, data, false, null);
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
}
