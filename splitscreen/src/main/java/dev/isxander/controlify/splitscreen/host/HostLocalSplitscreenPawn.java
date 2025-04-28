package dev.isxander.controlify.splitscreen.host;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class HostLocalSplitscreenPawn extends LocalSplitscreenPawn {
    public HostLocalSplitscreenPawn(Minecraft minecraft, @Nullable ControllerUID associatedController) {
        super(minecraft, associatedController);
    }

    /**
     * This is a no-op because the host does not need to join a server.
     * The host is always the server.
     * @param host address of the server
     * @param port port of the server
     */
    @Override
    public void joinServer(String host, int port) {
        // no-op
    }
}
