package dev.isxander.controlify.mixins.feature.reacharound;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.reacharound.ReachAroundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;

    @ModifyExpressionValue(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;"))
    private HitResult modifyPick(HitResult hitResult) {
        return ReachAroundHandler.getReachAroundHitResult(minecraft.getCameraEntity(), hitResult);
    }
}
