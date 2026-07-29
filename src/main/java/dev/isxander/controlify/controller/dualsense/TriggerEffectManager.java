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
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Function;

public class TriggerEffectManager {
	private final Minecraft minecraft;

	private final Map<DataComponentType<?>, Function<Object, DualsenseTriggerEffect>> useItemComponentEffects;
	private final Map<DataComponentType<?>, Function<Object, DualsenseTriggerEffect>> swingItemComponentEffects;

	public TriggerEffectManager(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.useItemComponentEffects = new LinkedHashMap<>();
		this.swingItemComponentEffects = new LinkedHashMap<>();
	}

	public void applyTriggerEffects(ControllerEntity controller) {
		controller.dualSense().ifPresent(ds -> {
			ds.setLeftTriggerEffect(this.getCurrentLeftTriggerEffect(controller));
			ds.setRightTriggerEffect(this.getCurrentRightTriggerEffect(controller));
		});
	}

	public <T> void registerUseItemComponentEffect(DataComponentType<T> componentType, Function<? super T, DualsenseTriggerEffect> effectFunction) {
		this.useItemComponentEffects.put(componentType, (Function<Object, DualsenseTriggerEffect>) effectFunction);
	}

	public <T> void registerSwingItemComponentEffect(DataComponentType<T> componentType, Function<? super T, DualsenseTriggerEffect> effectFunction) {
		this.swingItemComponentEffects.put(componentType, (Function<Object, DualsenseTriggerEffect>) effectFunction);
	}

	public DualsenseTriggerEffect getCurrentLeftTriggerEffect(ControllerEntity controller) {
		if (!this.shouldUseTriggerEffects(controller)) {
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

	public DualsenseTriggerEffect getCurrentRightTriggerEffect(ControllerEntity controller) {
		if (!this.shouldUseTriggerEffects(controller)) {
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

	public boolean shouldUseTriggerEffects(ControllerEntity controller) {
		return MinecraftUtil.getScreen() == null
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
		for (var entry : this.useItemComponentEffects.entrySet()) {
			if (stack.has(entry.getKey())) {
				Function<Object, DualsenseTriggerEffect> effectFunction = entry.getValue();
				DualsenseTriggerEffect effect = effectFunction.apply(stack.get(entry.getKey()));
				if (effect != null) {
					return Optional.of(effect);
				}
			}
		}

		var effectHolder = (TriggerEffectHolder) stack.getItem();
		return effectHolder.controlify$getUseTriggerEffect();
	}

	public Optional<DualsenseTriggerEffect> getSwingItemTriggerEffect(LocalPlayer player) {
		return this.getSwingItemTriggerEffect(player.getMainHandItem());
	}

	public Optional<DualsenseTriggerEffect> getSwingItemTriggerEffect(ItemStack stack) {
		for (var entry : this.swingItemComponentEffects.entrySet()) {
			if (stack.has(entry.getKey())) {
				Function<Object, DualsenseTriggerEffect> effectFunction = entry.getValue();
				DualsenseTriggerEffect effect = effectFunction.apply(stack.get(entry.getKey()));
				if (effect != null) {
					return Optional.of(effect);
				}
			}
		}

		var effectHolder = (TriggerEffectHolder) stack.getItem();
		return effectHolder.controlify$getSwingTriggerEffect();
	}


	private enum Trigger {
		LEFT, RIGHT, NEITHER
	}
}
