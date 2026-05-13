package dev.isxander.controlify.mixins.feature.bind;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.KeyMappingHandle;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BooleanSupplier;

@Mixin(ToggleKeyMapping.class)
public abstract class ToggleKeyMappingMixin extends KeyMappingMixin implements KeyMappingHandle {

    @Unique
    private BooleanSupplier controlifyToggleSupplier = () -> false;

    @Shadow
    public abstract void setDown(boolean value);

    @Override
    public void controlify$setPressed(boolean isDown) {
        if (isDown)
            this.incClickCount();
        this.setDown(isDown);
    }

    @Override
    public void controlify$forceSetPressed(boolean isDown) {
        super.controlify$setPressed(isDown);
    }

    @ModifyExpressionValue(method = "setDown", at = @At(value = "INVOKE", target = "Ljava/util/function/BooleanSupplier;getAsBoolean()Z"))
    private boolean modifyToggleMode(boolean vanillaToggleMode) {
        if (Controlify.instance().currentInputMode().isController()) {
            return controlifyToggleSupplier.getAsBoolean();
        }
        return vanillaToggleMode;
    }

    @Override
    public void controlify$addToggleCondition(ControllerEntity controller, BooleanSupplier condition) {
        BooleanSupplier oldCondition = controlifyToggleSupplier;
        controlifyToggleSupplier = () -> {
            boolean thisToggle = condition.getAsBoolean()
                                 && Controlify.instance().currentInputMode().isController()
                                 && Controlify.instance().getCurrentController().map(current -> controller == current).orElse(false);
            return oldCondition.getAsBoolean() || thisToggle;
        };
    }
}
