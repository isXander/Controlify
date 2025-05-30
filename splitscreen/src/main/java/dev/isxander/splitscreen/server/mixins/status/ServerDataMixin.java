package dev.isxander.splitscreen.server.mixins.status;

import dev.isxander.splitscreen.server.status.ServerStatusSplitscreenExt;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerData.class)
public class ServerDataMixin implements ServerStatusSplitscreenExt.Duck {
    @Unique
    private ServerStatusSplitscreenExt splitscreen$ext;

    @Override
    public ServerStatusSplitscreenExt splitscreen$getExt() {
        return splitscreen$ext;
    }

    @Override
    public void splitscreen$setExt(ServerStatusSplitscreenExt ext) {
        splitscreen$ext = ext;
    }
}
