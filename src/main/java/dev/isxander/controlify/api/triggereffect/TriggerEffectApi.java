/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.triggereffect;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class TriggerEffectApi {

	private TriggerEffectApi() {
	}

	public static <T> void registerUseItemEffect(DataComponentType<T> componentType, Function<? super T, @NotNull DualsenseTriggerEffect> effectFunction) {
		Controlify.instance().triggerEffectRegistry().registerUseItemComponentEffect(componentType, effectFunction);
	}

	public static void registerUseItemEffect(DataComponentType<?> componentType, @NotNull DualsenseTriggerEffect effect) {
		registerUseItemEffect(componentType, _ -> effect);
	}

	public static <T> void registerSwingItemEffect(DataComponentType<T> componentType, Function<? super T, @NotNull DualsenseTriggerEffect> effectFunction) {
		Controlify.instance().triggerEffectRegistry().registerSwingItemComponentEffect(componentType, effectFunction);
	}

	public static void registerSwingItemEffect(DataComponentType<?> componentType, @NotNull DualsenseTriggerEffect effect) {
		registerSwingItemEffect(componentType, _ -> effect);
	}

	public static void registerUseItemEffect(Item item, @NotNull DualsenseTriggerEffect effect) {
		Controlify.instance().triggerEffectRegistry().registerUseItemEffect(item, effect);
	}

	public static void registerSwingItemEffect(Item item, @NotNull DualsenseTriggerEffect effect) {
		Controlify.instance().triggerEffectRegistry().registerSwingItemEffect(item, effect);
	}
}
