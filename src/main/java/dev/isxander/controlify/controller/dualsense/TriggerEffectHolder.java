/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
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
