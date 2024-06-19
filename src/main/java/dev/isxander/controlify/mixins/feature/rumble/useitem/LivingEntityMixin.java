package dev.isxander.controlify.mixins.feature.rumble.useitem;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
            method = "startUsingItem",
            at = @At(
                    value = "INVOKE",
                    //? if >1.20.6 {
                    /*target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"
                    *///?} else {
                    target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"
                    //?}
            )
    )
    protected void onStartUsingItem(InteractionHand hand, CallbackInfo ci, @Local ItemStack stack) {

    }

    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    protected void onStopUsingItem(CallbackInfo ci) {

    }

    @Inject(method = "updateUsingItem", at = @At("HEAD"))
    protected void onUpdateUsingItem(ItemStack stack, CallbackInfo ci) {

    }
}
