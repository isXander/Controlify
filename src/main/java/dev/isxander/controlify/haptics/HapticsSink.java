package dev.isxander.controlify.haptics;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.dualsense.DS5TriggerEffect;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.haptics.hd.source.HDHapticsSource;
import dev.isxander.controlify.haptics.rumble.RumbleEffect;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

public interface HapticsSink {

    void acceptHDHaptic(HDHapticsSource HDHapticsSource);

    /**
     * Accepts a left trigger effect
     * @param triggerEffect the effect to apply to the left trigger
     */
    void acceptLeftTriggerEffect(DS5TriggerEffect triggerEffect);

    /**
     * Accepts a right trigger effect
     * @param triggerEffect the effect to apply to the right trigger
     */
    void acceptRightTriggerEffect(DS5TriggerEffect triggerEffect);

    /**
     * Accepts a left trigger effect only if the binding is bound to the left trigger
     * @param triggerEffect the effect to apply to the left trigger
     * @param forBinding the binding this effect is associated with
     */
    default void acceptLeftTriggerEffect(DS5TriggerEffect triggerEffect, InputBinding forBinding) {
        if (forBinding.boundInput().getRelevantInputs().contains(GamepadInputs.LEFT_TRIGGER_AXIS)) {
            this.acceptLeftTriggerEffect(triggerEffect);
        }
    }
    /**
     * Accepts a right trigger effect only if the binding is bound to the right trigger
     * @param triggerEffect the effect to apply to the right trigger
     * @param forBinding the binding this effect is associated with
     */
    default void acceptRightTriggerEffect(DS5TriggerEffect triggerEffect, InputBinding forBinding) {
        if (forBinding.boundInput().getRelevantInputs().contains(GamepadInputs.RIGHT_TRIGGER_AXIS)) {
            this.acceptRightTriggerEffect(triggerEffect);
        }
    }

    /**
     * Accepts a trigger effect for either the left or right depending on which trigger
     * is bound to the binding. If no trigger is bound to the binding, then the effect is not applied.
     * @param triggerEffect the effect to apply
     * @param forBinding the binding associated with this effect
     */
    default void acceptTriggerEffect(DS5TriggerEffect triggerEffect, InputBinding forBinding) {
        List<Identifier> relevantInputs = forBinding.boundInput().getRelevantInputs();
        if (relevantInputs.contains(GamepadInputs.LEFT_TRIGGER_AXIS)) {
            this.acceptLeftTriggerEffect(triggerEffect);
        } else if (relevantInputs.contains(GamepadInputs.RIGHT_TRIGGER_AXIS)) {
            this.acceptRightTriggerEffect(triggerEffect);
        }
    }

    /**
     * Accepts a rumble effect
     * @param rumbleEffect the rumble effect to apply
     */
    void acceptRumble(RumbleEffect rumbleEffect);
}
