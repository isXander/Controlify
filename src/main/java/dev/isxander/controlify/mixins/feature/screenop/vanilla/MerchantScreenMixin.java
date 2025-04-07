package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MerchantScreen.class)
public class MerchantScreenMixin {
    @Shadow
    private int scrollOff;
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
    private boolean accumulateScrolling(boolean canScroll, @Local(ordinal = 3, argsOnly = true) LocalDoubleRef scrollYRef) {
        if (canScroll) {
            // we want to target the beginning of the inner if statement,
            // so just check if the condition is true and do our stuff there

            double scrollY = scrollYRef.get();

            // checks if the scroll isn't 0 but will be cast to zero by the clamp in the target method
            if (((int) scrollY) == 0 && scrollY != 0) {
                if (Math.signum(accumulatedScroll) != Math.signum(scrollY)) {
                    // if the scroll direction is different, we want to reset the accumulated scroll
                    accumulatedScroll = 0;
                }

                // Add that decimal to the accumulated scroll
                accumulatedScroll += scrollY;
            }

            // mutate the local
            scrollYRef.set(accumulatedScroll);
        }

        // return the original value
        return canScroll;
    }

    /**
     * Reset the accumulated scroll if it is consumed by the scroll offset
     */
    @ModifyExpressionValue(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private int resetAccumulatedScroll(int original, @Local(ordinal = 3, argsOnly = true) double scrollY) {
        // This was copied from the mouseScrolled method
        int relativeScroll = (int) ((double) this.scrollOff - scrollY);

        // After the scroll is actually applied, we don't want to remember that accumulation
        this.accumulatedScroll -= relativeScroll;

        return original;
    }
}
