/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.api.contextual.TriggerEffectInstance;
import dev.isxander.controlify.contextual.ContextualDomains;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;

import java.util.*;

public class TriggerEffectManager {

	private final TriggerEffectInstance<InGameContext> instance;

	public TriggerEffectManager() {
		this.instance = ContextualDomains.INSTANCE.inGame().createTriggerEffectInstance();
	}

	public void applyTriggerEffects(
			ControllerEntity controller,
			boolean inputSuppressed
	) {
		applyTriggerEffects(controller, instance, inputSuppressed);
	}

	public void tick(Minecraft minecraft, ControllerEntity controller) {
		if (minecraft.player == null || minecraft.level == null) {
			return;
		}

		this.instance.update(
				InGameContext.create(minecraft, controller)
		);
	}

	public static void applyTriggerEffects(
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

	public static boolean shouldUseTriggerEffects(ControllerEntity controller, boolean inputSuppressed) {
		return !inputSuppressed
			&& MinecraftUtil.getScreen() == null
			&& Minecraft.getInstance().player != null
			&& controller.dualSense().map(ds -> ds.settings().triggerEffects).orElse(false);
	}
}
