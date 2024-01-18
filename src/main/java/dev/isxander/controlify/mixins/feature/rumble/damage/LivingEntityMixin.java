package dev.isxander.controlify.mixins.feature.rumble.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow
    public int hurtDuration;

    @Shadow
    public int hurtTime;

    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    protected void onHealthChangedDamage(DamageSource source, CallbackInfo ci) {

    }
}
