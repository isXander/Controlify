package dev.isxander.splitscreen.client.host;

import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.LocalSplitscreenPawn;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class HostLocalSplitscreenPawn extends LocalSplitscreenPawn {
    public HostLocalSplitscreenPawn(Minecraft minecraft, InputMethod associatedInputMethod) {
        super(minecraft, 0, associatedInputMethod);
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
    public void onConfigSave(Identifier config) {
        // no-op
    }
}
