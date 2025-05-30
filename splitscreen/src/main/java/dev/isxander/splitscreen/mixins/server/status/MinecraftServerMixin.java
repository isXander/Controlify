package dev.isxander.splitscreen.mixins.server.status;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.splitscreen.server.login.SplitscreenLoginFlowServer;
import dev.isxander.splitscreen.server.status.ServerStatusSplitscreenExt;
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
        var splitscreenExt = SplitscreenLoginFlowServer.buildSplitscreenStatus();
        return ServerStatusSplitscreenExt.construct(operation::call, splitscreenExt);
    }
}
