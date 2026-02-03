package dev.isxander.controlify.mixins.feature.screenop.impl.sign;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HangingSignEditScreen.class)
public abstract class HangingSignEditScreenMixin extends AbstractSignEditScreenMixin {

    protected HangingSignEditScreenMixin(Component title) {
        super(title);
    }

    //? if <1.21.6 {
    /*@WrapOperation(method = "offsetSign", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void modifySignOffset(PoseStack instance, float x, float y, float z, Operation<Void> original, @Local(argsOnly = true) BlockState state) {
        if (this.keyboard != null) {
            y = 70f;
        }
        original.call(instance, x, y, z);
    }
    *///?}
}
