/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class TriggerEffectManager {
	private final Minecraft minecraft;
	private final TriggerEffectRegistry registry;

	public TriggerEffectManager(Minecraft minecraft, TriggerEffectRegistry registry) {
		this.minecraft = minecraft;
		this.registry = registry;
	}

	public void applyTriggerEffects(ControllerEntity controller, boolean inputSuppressed) {
		controller.dualSense().ifPresent(ds -> {
			ds.setLeftTriggerEffect(this.getCurrentLeftTriggerEffect(controller, inputSuppressed));
			ds.setRightTriggerEffect(this.getCurrentRightTriggerEffect(controller, inputSuppressed));
		});
	}

	public DualsenseTriggerEffect getCurrentLeftTriggerEffect(ControllerEntity controller, boolean inputSuppressed) {
		if (!this.shouldUseTriggerEffects(controller, inputSuppressed)) {
			return DualsenseTriggerEffect.Off.INSTANCE;
		}

		LocalPlayer player = Objects.requireNonNull(this.minecraft.player);

		if (this.getUseItemTrigger(controller) == Trigger.LEFT) {
			return this.getUseItemTriggerEffect(player)
				.orElse(DualsenseTriggerEffect.Off.INSTANCE);
		}
		if (this.getSwingItemTrigger(controller) == Trigger.LEFT) {
			return this.getSwingItemTriggerEffect(player)
				.orElse(DualsenseTriggerEffect.Off.INSTANCE);
		}

		return DualsenseTriggerEffect.Off.INSTANCE;
	}

	public DualsenseTriggerEffect getCurrentRightTriggerEffect(ControllerEntity controller, boolean inputSuppressed) {
		if (!this.shouldUseTriggerEffects(controller, inputSuppressed)) {
			return DualsenseTriggerEffect.Off.INSTANCE;
		}

		LocalPlayer player = Objects.requireNonNull(this.minecraft.player);

		if (this.getUseItemTrigger(controller) == Trigger.RIGHT) {
			return this.getUseItemTriggerEffect(player)
				.orElse(DualsenseTriggerEffect.Off.INSTANCE);
		}
		if (this.getSwingItemTrigger(controller) == Trigger.RIGHT) {
			return this.getSwingItemTriggerEffect(player)
				.orElse(DualsenseTriggerEffect.Off.INSTANCE);
		}

		return DualsenseTriggerEffect.Off.INSTANCE;
	}

	public boolean shouldUseTriggerEffects(ControllerEntity controller, boolean inputSuppressed) {
		return !inputSuppressed
			&& MinecraftUtil.getScreen() == null
			&& this.minecraft.player != null
			&& controller.dualSense().map(ds -> ds.settings().triggerEffects).orElse(false);
	}

	private Trigger getUseItemTrigger(ControllerEntity controller) {
		List<Identifier> relevantInputs = ControlifyBindings.USE.on(controller).boundInput().getRelevantInputs();

		if (relevantInputs.contains(GamepadInputs.LEFT_TRIGGER_AXIS)) {
			return Trigger.LEFT;
		} else if (relevantInputs.contains(GamepadInputs.RIGHT_TRIGGER_AXIS)) {
			return Trigger.RIGHT;
		} else {
			return Trigger.NEITHER;
		}
	}

	private Trigger getSwingItemTrigger(ControllerEntity controller) {
		List<Identifier> relevantInputs = ControlifyBindings.ATTACK.on(controller).boundInput().getRelevantInputs();

		if (relevantInputs.contains(GamepadInputs.LEFT_TRIGGER_AXIS)) {
			return Trigger.LEFT;
		} else if (relevantInputs.contains(GamepadInputs.RIGHT_TRIGGER_AXIS)) {
			return Trigger.RIGHT;
		} else {
			return Trigger.NEITHER;
		}
	}

	public Optional<DualsenseTriggerEffect> getUseItemTriggerEffect(LocalPlayer player) {
		return this.getUseItemTriggerEffect(player.getActiveItem())
			.or(() -> this.getUseItemTriggerEffect(player.getOffhandItem()));
	}

	public Optional<DualsenseTriggerEffect> getUseItemTriggerEffect(ItemStack stack) {
		return this.registry.getUseItemEffect(stack);
	}

	public Optional<DualsenseTriggerEffect> getSwingItemTriggerEffect(LocalPlayer player) {
		return this.getSwingItemTriggerEffect(player.getMainHandItem());
	}

	public Optional<DualsenseTriggerEffect> getSwingItemTriggerEffect(ItemStack stack) {
		return this.registry.getSwingItemEffect(stack);
	}


	private enum Trigger {
		LEFT, RIGHT, NEITHER
	}
}
