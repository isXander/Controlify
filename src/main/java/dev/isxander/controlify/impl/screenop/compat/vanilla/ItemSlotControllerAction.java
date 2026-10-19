/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.impl.controller.ControllerImpl;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.item.ItemStack;

public interface ItemSlotControllerAction extends ItemSlotMouseAction {
	boolean controlify$onControllerInput(
			ItemStack stack,
			int hoveredSlotIndex,
			ControllerImpl controller
	);
}
