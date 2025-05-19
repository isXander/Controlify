package dev.isxander.controlify.splitscreen.mixins.server.status;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.splitscreen.server.login.SplitscreenLoginConfig;
import dev.isxander.controlify.splitscreen.server.status.ServerStatusSplitscreenExt;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    /**
     * Construct this server status with the splitscreen extension.
     * @param operation the original constructor call
     * @return the constructed server status
     */
    @WrapMethod(method = "buildServerStatus")
    private ServerStatus addSplitscreenInfoToServerStatus(Operation<ServerStatus> operation) {
        var splitscreenExt = new ServerStatusSplitscreenExt(new int[]{1}, SplitscreenLoginConfig.MAX_CLIENTS);
        return ServerStatusSplitscreenExt.construct(operation::call, splitscreenExt);
    }
}
