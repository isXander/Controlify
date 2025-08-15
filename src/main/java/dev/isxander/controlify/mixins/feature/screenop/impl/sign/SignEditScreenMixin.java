package dev.isxander.controlify.mixins.feature.screenop.impl.sign;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SignEditScreen.class)
public abstract class SignEditScreenMixin extends AbstractSignEditScreen {

    public SignEditScreenMixin(SignBlockEntity sign, boolean isFrontText, boolean isFiltered) {
        super(sign, isFrontText, isFiltered);
    }

    @ModifyReturnValue(method = "getSignYOffset", at = @At("RETURN"))
    private float modifySignY(float original) {
        return original - calculateOverlap();
    }

    @Definition(id = "submitSignRenderState", method = "Lnet/minecraft/client/gui/GuiGraphics;submitSignRenderState(Lnet/minecraft/client/model/Model$Simple;FLnet/minecraft/world/level/block/state/properties/WoodType;IIII)V")
    @Expression("?.submitSignRenderState(?, ?, ?, ?, @(66), ?, @(168))")
    @ModifyExpressionValue(method = "renderSignBackground", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifySignRenderY(int original) {
        return (int) (original - calculateOverlap());
    }

    @Unique
    private float calculateOverlap() {
        float original = 90f;

        float keyboardStart = this.height / 2f;
        float signEnd = original + 90;
        return Math.max(0, signEnd - keyboardStart);
    }

}
