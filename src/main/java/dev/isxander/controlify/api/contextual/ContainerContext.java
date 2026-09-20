/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ContainerContext(
		Player player,
		@Nullable Slot hoveredSlot,
		ItemStack holdingItem,
		boolean cursorOutsideContainer,
		ControllerEntity controller,
		GuideVerbosity verbosity
) implements Context {
}
