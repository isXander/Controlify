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
import net.minecraft.world.item.Items;

public final class BuiltinTriggerEffects {
	private BuiltinTriggerEffects() {
	}

	public static void register() {
		var quickClick = new DualsenseTriggerEffect.Weapon((byte) 3, (byte) 5, (byte) 1);

		TriggerEffectApi.registerUseItemEffect(
			Items.BOW,
			new DualsenseTriggerEffect.FeedbackSlope((byte) 3, (byte) 9, (byte) 2, (byte) 8)
		);

		TriggerEffectApi.registerSwingItemEffect(
			DataComponents.WEAPON,
			quickClick
		);

		TriggerEffectApi.registerUseItemEffect(
			DataComponents.CHARGED_PROJECTILES,
			chargedProjectiles -> chargedProjectiles.isEmpty()
				? new DualsenseTriggerEffect.FeedbackSlope((byte) 2, (byte) 9, (byte) 5, (byte) 8)
				: quickClick
		);

		TriggerEffectApi.registerUseItemEffect(
			DataComponents.CONSUMABLE,
			new DualsenseTriggerEffect.Feedback((byte) 3, (byte) 1)
		);

		TriggerEffectApi.registerUseItemEffect(
			DataComponents.BLOCKS_ATTACKS,
			new DualsenseTriggerEffect.Feedback((byte) 3, (byte) 3)
		);

		TriggerEffectApi.registerUseItemEffect(
			DataComponents.EQUIPPABLE,
			quickClick
		);

		TriggerEffectApi.registerUseItemEffect(
			DataComponents.KINETIC_WEAPON,
			new DualsenseTriggerEffect.Feedback((byte) 3, (byte) 3)
		);

		TriggerEffectApi.registerUseItemEffect(
			DataComponents.INSTRUMENT,
			new DualsenseTriggerEffect.Feedback((byte) 3, (byte) 3)
		);
	}
}
