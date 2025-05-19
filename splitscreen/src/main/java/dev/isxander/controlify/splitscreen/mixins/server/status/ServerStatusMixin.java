package dev.isxander.controlify.splitscreen.mixins.server.status;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import dev.isxander.controlify.splitscreen.server.status.ServerStatusSplitscreenExt;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerStatus.class)
public class ServerStatusMixin implements ServerStatusSplitscreenExt.Duck {

    @Unique
    private final ServerStatusSplitscreenExt ext = ServerStatusSplitscreenExt.inProgressParam.get();

    @Override
    public ServerStatusSplitscreenExt splitscreen$getExt() {
        return ext;
    }

    @Override
    public void splitscreen$setExt(ServerStatusSplitscreenExt ext) {
        throw new UnsupportedOperationException("Cannot set ext on records");
    }

    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
            )
    )
    private static Codec<ServerStatus> wrapCodec(Codec<ServerStatus> codec) {
        return ServerStatusSplitscreenExt.wrapCodec(codec);
    }
}
