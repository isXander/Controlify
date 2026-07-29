/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.api.triggereffect.TriggerEffectApi;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import net.minecraft.core.component.DataComponents;

public final class BuiltinTriggerEffects {
	private BuiltinTriggerEffects() {
	}

	public static void register() {
		var quickClick = new DualsenseTriggerEffect.Weapon((byte) 3, (byte) 5, (byte) 1);

		TriggerEffectApi.registerUseItemEffect(
			DataComponents.CHARGED_PROJECTILES,
			chargedProjectiles -> chargedProjectiles.isEmpty()
				? new DualsenseTriggerEffect.FeedbackSlope((byte) 2, (byte) 9, (byte) 5, (byte) 8)
				: quickClick
		);
	}
}
