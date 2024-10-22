package dev.isxander.controlify.mixins.feature.patches.boatfix;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.fixes.boatfix.AnalogBoatInput;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        //? if >=1.21.2 {
        net.minecraft.world.entity.vehicle.AbstractBoat.class
        //?} else {
        /*net.minecraft.world.entity.vehicle.Boat.class
        *///?}
)
public abstract class BoatMixin implements AnalogBoatInput {
    @Shadow private float deltaRotation;
    @Shadow private boolean inputLeft;
    @Shadow private boolean inputRight;
    @Shadow private boolean inputUp;
    @Shadow private boolean inputDown;

    @Unique private float analogForward;
    @Unique private float analogRight;
    @Unique private boolean usingAnalogInput;

    @Unique
    private static final String TARGET_CLASS =
            //? if >=1.21.2 {
            "Lnet/minecraft/world/entity/vehicle/AbstractBoat";
            //?} else {
            /*"Lnet/minecraft/world/entity/vehicle/Boat";
            *///?}

    @Inject(method = "controlBoat", at = @At(value = "INVOKE", target = TARGET_CLASS + ";getYRot()F", ordinal = 0))
    private void rotateBoatAnalog(CallbackInfo ci) {
        if (usingAnalogInput)
            this.deltaRotation += analogRight;
    }

    @ModifyVariable(method = "controlBoat", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private float forwardBoatAnalog(float forwardVelocity) {
        if (!usingAnalogInput)
            return forwardVelocity;

        // these values are what vanilla boat uses
        float velocity = analogForward > 0 ? analogForward * 0.04f : analogForward * 0.005f;

        return forwardVelocity + velocity;
    }

    @ModifyExpressionValue(method = "controlBoat", at = {
            @At(value = "FIELD", target = TARGET_CLASS + ";inputLeft:Z", opcode = Opcodes.GETFIELD, ordinal = 0),
            @At(value = "FIELD", target = TARGET_CLASS + ";inputRight:Z", opcode = Opcodes.GETFIELD, ordinal = 0),
            @At(value = "FIELD", target = TARGET_CLASS + ";inputUp:Z", opcode = Opcodes.GETFIELD, ordinal = 1),
            @At(value = "FIELD", target = TARGET_CLASS + ";inputDown:Z", opcode = Opcodes.GETFIELD, ordinal = 1)
    })
    private boolean shouldDoDigitalInput(boolean original) {
        return !usingAnalogInput && original;
    }

    @Override
    public void controlify$setAnalogInput(float forward, float right) {
        this.usingAnalogInput = true;

        this.analogForward = forward;
        this.analogRight = right;

        this.inputLeft = right < 0;
        this.inputRight = right > 0;
        this.inputUp = forward > 0;
        this.inputDown = forward < 0;
    }

    @Inject(method = "setInput", at = @At("HEAD"))
    private void onUseDigitalInput(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack, CallbackInfo ci) {
        this.usingAnalogInput = false;
    }
}
