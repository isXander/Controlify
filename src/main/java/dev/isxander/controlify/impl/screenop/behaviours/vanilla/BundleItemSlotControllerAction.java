/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

public class BundleItemSlotControllerAction {
	public static boolean onControllerInput(
			ItemStack stack,
			int hoveredSlotIndex,
			ControllerImpl controller,
			SelectedBundleItemConsumer consumer
	) {
		int uniqueItems = BundleItem.getNumberOfItemsToShow(stack);

		if (uniqueItems > 0) {
			dev.isxander.controlify.impl.screenop.ScreenRuntime.virtualMouse().ifPresent(mouse -> mouse.preventScrollingThisTick());

			boolean handled = false;
			for (var direction : ScreenDirection.values()) {
				var action = switch (direction) {
					case UP -> ControlifyActions.GUI_SECONDARY_NAVI_UP;
					case DOWN -> ControlifyActions.GUI_SECONDARY_NAVI_DOWN;
					case LEFT -> ControlifyActions.GUI_SECONDARY_NAVI_LEFT;
					case RIGHT -> ControlifyActions.GUI_SECONDARY_NAVI_RIGHT;
				};
				while (action.on(controller).pulse().consumePulse()) {
					int offsetX = direction == ScreenDirection.LEFT ? -1
							: direction == ScreenDirection.RIGHT ? 1 : 0;
					int offsetY = direction == ScreenDirection.UP ? -1
							: direction == ScreenDirection.DOWN ? 1 : 0;
					handled |= moveSelection(stack, hoveredSlotIndex, consumer, uniqueItems, offsetX, offsetY);
				}
			}
			return handled;
		}

		return false;
	}

	private static boolean moveSelection(ItemStack stack, int hoveredSlotIndex, SelectedBundleItemConsumer consumer, int uniqueItems, int offsetX, int offsetY) {
		int currentIndex = BundleItem.getSelectedItemIndex(stack);

		if (currentIndex == -1) {
			consumer.accept(stack, hoveredSlotIndex, 0);
			return true;
		}

		/*
		Bundle grid is organised where the first row is the incomplete row and is RTL,
		- - 0 1
		2 3 4 5
		 */
		int rowSize = BundleItem.MAX_SHOWN_GRID_ITEMS_X;
		int colSize = Math.min(Mth.ceil(uniqueItems / (float) rowSize), BundleItem.MAX_SHOWN_GRID_ITEMS_Y);
		int incompleteRowSize = uniqueItems % rowSize;
		int emptySlots = (rowSize - incompleteRowSize) % rowSize;

		int gridX = (currentIndex + emptySlots) % rowSize;
		int gridY = (currentIndex + emptySlots) / rowSize;

		int newGridX = (gridX + offsetX + rowSize) % rowSize;
		int newGridY = (gridY + offsetY + colSize) % colSize;

		if (newGridY >= 0 && newGridY < colSize) {
			int newIndex = Math.max(newGridX + newGridY * rowSize - emptySlots, 0);

			consumer.accept(stack, hoveredSlotIndex, newIndex);
		}

		return true;
	}

	public interface SelectedBundleItemConsumer {
		void accept(ItemStack stack, int hoveredSlot, int selectedIndex);
	}
}
