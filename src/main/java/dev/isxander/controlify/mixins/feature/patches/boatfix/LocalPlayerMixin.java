package dev.isxander.controlify.mixins.feature.patches.boatfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.fixes.boatfix.AnalogBoatInput;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Shadow public Input input;

    @WrapOperation(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setInput(ZZZZ)V"))
    private void useAnalogInput(Boat boat, boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack, Operation<Void> original) {
        if (ControlifyApi.get().currentInputMode().isController() && !Controlify.instance().config().globalSettings().shouldUseKeyboardMovement()) {
            ((AnalogBoatInput) boat).controlify$setAnalogInput(
                    input.forwardImpulse,
                    -input.leftImpulse
            );

            return;
        }

        original.call(boat, pressingLeft, pressingRight, pressingForward, pressingBack);
    }
}
