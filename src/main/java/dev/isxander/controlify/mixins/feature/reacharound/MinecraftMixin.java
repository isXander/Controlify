package dev.isxander.controlify.mixins.feature.reacharound;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.reacharound.ReachAroundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public abstract @Nullable Entity getCameraEntity();

    @ModifyExpressionValue(
            method = "pick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;"
            )
    )
    private HitResult modifyPick(HitResult hitResult) {
        return ReachAroundHandler.getReachAroundHitResult(this.getCameraEntity(), hitResult);
    }
}
