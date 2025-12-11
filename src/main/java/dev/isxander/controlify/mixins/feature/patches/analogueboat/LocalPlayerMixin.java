package dev.isxander.controlify.mixins.feature.patches.analogueboat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.fixes.boatfix.AnalogBoatInput;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.MthExt;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

//? if >=1.21.11 {
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
//?} else {
/*import net.minecraft.world.entity.vehicle.Boat;
//? if >=1.21.2 {
import net.minecraft.world.entity.vehicle.AbstractBoat;
//?}
*///?}

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    //? if >=1.21.2 {
    @Shadow public net.minecraft.client.player.ClientInput input;
    //?} else {
    /*@Shadow public net.minecraft.client.player.Input input;
    *///?}

    @WrapOperation(
            method = "rideTick",
            at = @At(
                    value = "INVOKE",
                    //? if >=1.21.11 {
                    target = "Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;setInput(ZZZZ)V"
                    //?} elif >=1.21.2 {
                    /*target = "Lnet/minecraft/world/entity/vehicle/AbstractBoat;setInput(ZZZZ)V"
                    *///?} else {
                    /*target = "Lnet/minecraft/world/entity/vehicle/Boat;setInput(ZZZZ)V"
                    *///?}
            )
    )
    private void useAnalogInput(
            //? if >=1.21.2 {
            AbstractBoat boat,
            //?} else {
            /*Boat boat,
            *///?}
            boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack,
            Operation<Void> original
    ) {
        if (ControlifyApi.get().currentInputMode().isController() && !Controlify.instance().config().globalSettings().shouldUseKeyboardMovement()) {
            Vec2 moveVec = InGameInputHandler.getMoveVec(input);
            float forwardImpulse = moveVec.y;
            float rightImpulse = -moveVec.x;

            // Add a deadzone to the analog input. Right impulse between -0.1 and 0.1 is considered 0.
            // Other values are remapped to remap [0.1, 1] to [0, 1] and [-1, -0.1] to [-1, 0].
            float deadzone = 0.1f;

            float onlyRightImpulseAbs = Math.max(0, rightImpulse);
            float onlyLeftImpulseAbs = -Math.min(0, rightImpulse);

            onlyRightImpulseAbs = onlyRightImpulseAbs < deadzone ? 0 : MthExt.remap(onlyRightImpulseAbs, deadzone, 1, 0, 1);
            onlyLeftImpulseAbs = onlyLeftImpulseAbs < deadzone ? 0 : MthExt.remap(onlyLeftImpulseAbs, deadzone, 1, 0, 1);

            rightImpulse = onlyRightImpulseAbs - onlyLeftImpulseAbs;

            ((AnalogBoatInput) boat).controlify$setAnalogInput(
                    forwardImpulse,
                    rightImpulse
            );

            return;
        }

        original.call(boat, pressingLeft, pressingRight, pressingForward, pressingBack);
    }
}
