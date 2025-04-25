package dev.isxander.controlify.splitscreen.host;

import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import net.minecraft.client.Minecraft;

public class HostLocalSplitscreenPawn extends LocalSplitscreenPawn {
    public HostLocalSplitscreenPawn(Minecraft minecraft) {
        super(minecraft);
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
