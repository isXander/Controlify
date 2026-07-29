package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface TriggerEffectHolder {
	Optional<DualsenseTriggerEffect> controlify$getUseTriggerEffect();

	Optional<DualsenseTriggerEffect> controlify$getSwingTriggerEffect();

	void controlify$assignUseTriggerEffect(@NotNull DualsenseTriggerEffect effect);

	void controlify$assignSwingTriggerEffect(@NotNull DualsenseTriggerEffect effect);
}
