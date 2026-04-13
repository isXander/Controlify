package dev.isxander.controlify.compatibility.rrls.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.isxander.controlify.utils.CUtil;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "org.redlance.dima_dencep.mods.rrls.config.HideType")
public class HideTypeMixin {
    /**
     * RRLS calls onGameLoadFinished before resource loaders have finished.
     * This causes a crash because Controlify initialises on a hook into this method,
     * and expects DefaultConfigManager to have loaded default configs, causing the following crash:
     * <code>Attempted to fetch default config before DefaultConfigManager was ready!</code>
     * <p>
     * Developer was unwilling to add an official API for Controlify to use to accomplish this,
     * so this mixin prevents RRLS ever removing the loading screen from the initial load.
     */
    @Dynamic
    @ModifyReturnValue(method = "canHide", at = @At("RETURN"), require = 0)
    private boolean preventInitialHide(boolean original, boolean reloading) {
        if (!reloading && original) {
            CUtil.LOGGER.error("Controlify has prevented Remove Reloading Screen (rrls) mod from removing the initial loading screen due to an incompatibility.");
            return false;
        }

        return original;
    }
}
