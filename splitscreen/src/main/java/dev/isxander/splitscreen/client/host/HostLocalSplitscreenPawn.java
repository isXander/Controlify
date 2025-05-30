package dev.isxander.splitscreen.client.host;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.LocalSplitscreenPawn;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class HostLocalSplitscreenPawn extends LocalSplitscreenPawn {
    public HostLocalSplitscreenPawn(Minecraft minecraft, @Nullable ControllerUID associatedController) {
        super(minecraft, 0, associatedController);
    }

    /**
     * This is a no-op because the host does not need to join a server.
     * The host is always the server.
     *
     * @param host  address of the server
     * @param port  port of the server
     * @param nonce the nonce for splitscreen authentication
     */
    @Override
    public void joinServer(String host, int port, byte @Nullable [] nonce) {
        // no-op
    }

    /**
     * This client was the one that published this event.
     * This would get recursive.
     * @param config the id of the config that was saved
     */
    @Override
    public void onConfigSave(ResourceLocation config) {
        // no-op
    }
}
