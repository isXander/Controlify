/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.api.contextual.TriggerEffectInstance;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;

import java.util.*;

public class TriggerEffectManager {
	private final Minecraft minecraft;

	public TriggerEffectManager(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void applyTriggerEffects(
			ControllerEntity controller,
			TriggerEffectInstance<?> instance,
			boolean inputSuppressed
	) {
		controller.dualSense().ifPresent(ds -> {
			if (!shouldUseTriggerEffects(controller, inputSuppressed)) {
				ds.setLeftTriggerEffect(DualsenseTriggerEffect.Off.INSTANCE);
				ds.setRightTriggerEffect(DualsenseTriggerEffect.Off.INSTANCE);
				return;
			}

			ds.setLeftTriggerEffect(instance.getLeftTriggerEffect());
			ds.setRightTriggerEffect(instance.getRightTriggerEffect());
		});
	}

	public boolean shouldUseTriggerEffects(ControllerEntity controller, boolean inputSuppressed) {
		return !inputSuppressed
			&& MinecraftUtil.getScreen() == null
			&& this.minecraft.player != null
			&& controller.dualSense().map(ds -> ds.settings().triggerEffects).orElse(false);
	}
}
