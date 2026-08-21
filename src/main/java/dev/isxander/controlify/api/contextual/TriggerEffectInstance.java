/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import org.jetbrains.annotations.ApiStatus;

public interface TriggerEffectInstance<C extends Context> extends ContextualInstance<C> {
	@ApiStatus.Experimental
	DualsenseTriggerEffect getLeftTriggerEffect();

	@ApiStatus.Experimental
	DualsenseTriggerEffect getRightTriggerEffect();
}
