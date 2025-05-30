package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MerchantScreen.class)
public class MerchantScreenMixin {
    @Unique private double accumulatedScroll = 0;

    /**
     * mouseScrolled casts the scroll amount to an integer.
     * Controlify vmouse scroll tends to be a decimal between -1 and 1,
     * so it ended up always casting to zero.
     * Here we accumulate that scroll amount over time and use that, so eventually it will
     * be enough to cast to 1 or -1.
     */
    @ModifyExpressionValue(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;canScroll(I)Z"))
    // target is just before we calculate new scroll offset
    private boolean accumulateScrolling(boolean canScroll, @Local(ordinal = 3, argsOnly = true) double scrollY) {
        if (canScroll) {
            // we want to target the beginning of the inner if statement,
            // so just check if the condition is true and do our stuff there

            double currentScrollSign = Math.signum(scrollY);
            double accScrollSign = Math.signum(accumulatedScroll);

            if (currentScrollSign != accScrollSign) {
                this.accumulatedScroll = scrollY;
            } else {
                this.accumulatedScroll += scrollY;
            }
        }

        // return the original value
        return canScroll;
    }

    @Definition(id = "clamp", method = "Lnet/minecraft/util/Mth;clamp(III)I")
    @Definition(id = "scrollOff", field = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;scrollOff:I")
    @Definition(id = "scrollY", local = @Local(ordinal = 3, argsOnly = true, type = double.class))
    @Expression("this.scrollOff = clamp((int) ((double) this.scrollOff - @(scrollY)), ?, ?)")
    @ModifyExpressionValue(method = "mouseScrolled", at = @At("MIXINEXTRAS:EXPRESSION"))
    private double useAccumulatedScrollField(double scrollY) {
        double prev = this.accumulatedScroll;
        this.accumulatedScroll -= (int) prev;
        return prev;
    }

}
