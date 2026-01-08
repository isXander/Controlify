package dev.isxander.controlify.mixins.feature.patches.sprintfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.controlify.Controlify;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    /**
     * In vanilla, sprinting can start when the forward impulse is above just
     * <code>0.00001</code>. This makes little sense when impl players have toggle-sprint
     * enabled, and they're creeping around with minimal input.
     * Making this <code>>=0.8</code> instead of <code>>0.00001</code> makes a lot more sense.
     * <p>
     * There is no need to hide this behind keyboard movement setting, since keyboards will only ever see a
     * y of <code>0</code> or <code>1</code>, so the point in between where sprinting begins is irrelevant.
     * However, we do so anyway just for the sake of preventing any modifications to movement code with this toggle on.
     * <p>
     * This mixin retains parity with <1.21.5, where sprinting was only allowed at >=0.8f, 1.21.5 removed this,
     * since it does nto affect vanilla keyboard players.
     *
     * @param instance the receiver
     * @param original the original call to {@link net.minecraft.client.player.ClientInput#hasForwardImpulse()}
     * @return if the client input state satisfies sprinting
     */
    //? if >=1.21.5 {
    @WrapOperation(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;hasForwardImpulse()Z"))
    private boolean requireHalfImpulse(net.minecraft.client.player.ClientInput instance, Operation<Boolean> original) {
        if (Controlify.instance().currentInputMode().isController()
                && !Controlify.instance().config().globalSettings().shouldUseKeyboardMovement()) {
            return instance.getMoveVector().y >= 0.8f;
        }

        return original.call(instance);
    }
    //?}
}
