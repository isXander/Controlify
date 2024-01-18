package dev.isxander.controlify.mixins.feature.rumble.damage;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin extends LivingEntityMixin {
    @Inject(method = "animateHurt", at = @At("HEAD"))
    protected void onEntityHurtMeDamage(float yaw, CallbackInfo ci) {

    }
}
