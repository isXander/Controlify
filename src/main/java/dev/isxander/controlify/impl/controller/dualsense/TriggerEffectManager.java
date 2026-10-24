/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.dualsense;

import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.api.contextual.TriggerEffectInstance;
import dev.isxander.controlify.impl.contextual.ContextualDomains;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;

public class TriggerEffectManager {

	private final TriggerEffectInstance<InGameContext> instance;

	public TriggerEffectManager() {
		this.instance = ContextualDomains.INSTANCE.inGame().createTriggerEffectInstance();
	}

	public void applyTriggerEffects(
			ControllerImpl controller,
			boolean inputSuppressed
	) {
		applyTriggerEffects(controller, instance, inputSuppressed);
	}

	public void tick(Minecraft minecraft, ControllerImpl controller) {
		if (minecraft.player == null || minecraft.level == null) {
			return;
		}

		this.instance.update(
				InGameContext.create(minecraft, controller)
		);
	}

	public static void applyTriggerEffects(
			ControllerImpl controller,
			TriggerEffectInstance<?> instance,
			boolean inputSuppressed
	) {
		controller.dualsense().ifPresent(ds -> {
			if (!shouldUseTriggerEffects(controller, inputSuppressed)) {
				ds.setLeftTriggerEffect(DualsenseTriggerEffect.Off.INSTANCE);
				ds.setRightTriggerEffect(DualsenseTriggerEffect.Off.INSTANCE);
				return;
			}

			ds.setLeftTriggerEffect(instance.getLeftTriggerEffect());
			ds.setRightTriggerEffect(instance.getRightTriggerEffect());
		});
	}

	public static boolean shouldUseTriggerEffects(ControllerImpl controller, boolean inputSuppressed) {
		return !inputSuppressed
			&& MinecraftUtil.getScreen() == null
			&& Minecraft.getInstance().player != null
			&& controller.dualsense().map(ds -> ds.settings().triggerEffects).orElse(false);
	}
}
