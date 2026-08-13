package dev.isxander.controlify.api.contextual;

import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import org.jetbrains.annotations.ApiStatus;

public interface TriggerEffectInstance<C extends Context> extends ContextualInstance<C> {
	@ApiStatus.Experimental
	DualsenseTriggerEffect getLeftTriggerEffect();

	@ApiStatus.Experimental
	DualsenseTriggerEffect getRightTriggerEffect();
}
