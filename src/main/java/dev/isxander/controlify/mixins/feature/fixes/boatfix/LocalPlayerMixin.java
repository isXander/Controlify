package dev.isxander.controlify.mixins.feature.fixes.boatfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.fixes.boatfix.AnalogBoatInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @WrapOperation(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setInput(ZZZZ)V"))
    private void useAnalogInput(Boat boat, boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack, Operation<Void> original) {
        if (ControlifyApi.get().currentInputMode() == InputMode.CONTROLLER && !Controlify.instance().config().globalSettings().keyboardMovement) {
            Optional<Controller<?, ?>> controllerOpt = ControlifyApi.get().getCurrentController();
            if (controllerOpt.isPresent()) {
                var controller = controllerOpt.get();

                ((AnalogBoatInput) boat).setAnalogInput(
                        controller.bindings().WALK_FORWARD.state() - controller.bindings().WALK_BACKWARD.state(),
                        controller.bindings().WALK_RIGHT.state() - controller.bindings().WALK_LEFT.state()
                );

                return;
            }
        }

        original.call(boat, pressingLeft, pressingRight, pressingForward, pressingBack);
    }
}
