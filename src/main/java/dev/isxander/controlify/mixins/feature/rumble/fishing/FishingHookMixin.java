package dev.isxander.controlify.mixins.feature.rumble.fishing;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingHookMixin {
    @Shadow private boolean biting;

    @Unique private boolean isLocalPlayerHook;
    @Unique private ContinuousRumbleEffect bitingRumble;

    @ModifyExpressionValue(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;get(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;", ordinal = 1))
    private Object onBitingStateUpdated(Object bitingObj) {
        var biting = (boolean) bitingObj;
        if (isLocalPlayerHook) {
            if (biting && !this.biting) {
                bitingRumble = ContinuousRumbleEffect.builder()
                        .constant(0f, 0.05f)
                        .build();
                ControlifyApi.get().currentController().rumbleManager().play(RumbleSource.MISC, bitingRumble);
            } else if (!biting && this.biting) {
                stopBitingRumble();
            }
        }
        return biting;
    }

    @Inject(method = "onClientRemoval", at = @At("RETURN"))
    private void onClientRemoval(CallbackInfo ci) {
        stopBitingRumble();
    }

    private void stopBitingRumble() {
        if (bitingRumble != null) {
            bitingRumble.stop();
            bitingRumble = null;
        }
    }

    @Inject(method = "setOwner", at = @At("RETURN"))
    private void onOwnerSet(@Nullable Entity entity, CallbackInfo ci) {
        isLocalPlayerHook = entity instanceof LocalPlayer;
    }
}
