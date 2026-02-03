package dev.isxander.splitscreen.client.mixins.controlify;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.config.ConfigManager;import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ConfigManager.class)
public class ControlifyConfigMixin {
    /**
     * Don't allow saving the config is we're a pawn client,
     * only the controller should modify controller config.
     *
     * @param original the original method call
     */
    @WrapMethod(method = "save")
    private boolean preventSaveIfPawn(Operation<Boolean> original) {
        if (SplitscreenBootstrapper.getPawn().isEmpty()) {
            return original.call();
        }
        return true;
    }
}
