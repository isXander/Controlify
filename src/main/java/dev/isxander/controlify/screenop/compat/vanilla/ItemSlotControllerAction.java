/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.item.ItemStack;

public interface ItemSlotControllerAction extends ItemSlotMouseAction {
	boolean controlify$onControllerInput(
			ItemStack stack,
			int hoveredSlotIndex,
			ControllerEntity controller
	);
}
