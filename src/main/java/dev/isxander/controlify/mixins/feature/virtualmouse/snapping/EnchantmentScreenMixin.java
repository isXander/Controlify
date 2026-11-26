/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.screenop.SnapPoint;
import dev.isxander.controlify.impl.screenop.behaviours.screen.AdditionalSnapPointsBehaviour;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> implements AdditionalSnapPointsBehaviour.Access {

	@Unique private static final int SLOT_AREA_OFFSET_X = 60, SLOT_AREA_OFFSET_Y = 14;
	@Unique private static final int SLOT_WIDTH = 108, SLOT_HEIGHT = 19;
	@Unique private static final int SLOT_VERTICAL_SPACING = SLOT_HEIGHT;

	// This math is based on the renderBg method in EnchantmentScreen
	protected EnchantmentScreenMixin(EnchantmentMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void controlify$collectSnapPoints(@NonNull Consumer<SnapPoint> consumer) {
		int leftPos = this.leftPos;
		int topPos = this.topPos;
		int snapRadius = (SLOT_HEIGHT / 2) + 5;

		// enchanting table has 3 options
		for (int slotIndex = 0; slotIndex < 3; slotIndex++) {
			if (this.menu.costs[slotIndex] == 0) {
				// Cost is 0 if there is no enchantment in this slot, skip
				continue;
			}

			// Purposefully include the slots even if the player can't afford them,
			// since you can still hover over them to see the enchantment and its cost.
			int slotX = leftPos + SLOT_AREA_OFFSET_X;
			int slotY = topPos + SLOT_AREA_OFFSET_Y + (slotIndex * SLOT_VERTICAL_SPACING);

			int centreSlotX = slotX + (SLOT_WIDTH / 2);
			int centreSlotY = slotY + (SLOT_HEIGHT / 2);

			// Add the snap point for this slot
			consumer.accept(new SnapPoint(centreSlotX, centreSlotY, snapRadius));
		}
	}
}
