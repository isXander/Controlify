/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.screenop.SnapPoint;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.CreativeModeInventoryScreenBehaviour;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Consumer;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements CreativeModeInventoryScreenBehaviour.Access {
	@Shadow
	protected abstract int getTabX(CreativeModeTab group);

	@Shadow
	private float scrollOffs;
	@Shadow
	private EditBox searchBox;

	@Shadow
	protected abstract boolean canScroll();

	protected CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void controlify$collectTabSnapPoints(List<CreativeModeTab> tabs, Consumer<SnapPoint> consumer) {

		for (var tab : tabs) {
			boolean topRow = tab.row() == CreativeModeTab.Row.TOP;
			int x = leftPos + getTabX(tab);
			int y = topPos + (topRow ? -28 : imageHeight - 4);

			consumer.accept(new SnapPoint(new Vector2i(x + 13, y + 16), 18));
		}

		if (canScroll()) {
			int scrollTop = topPos + 18;
			int scrollBottom = scrollTop + 112;
			consumer.accept(new SnapPoint(new Vector2i(leftPos + 175 + 6, scrollTop + (int) ((float) (scrollBottom - scrollTop - 17) * scrollOffs) + 7), 15));
		}

		if (searchBox.isVisible())
			consumer.accept(new SnapPoint(new Vector2i(searchBox.getX() + searchBox.getWidth() / 2, searchBox.getY() + searchBox.getHeight() / 2), searchBox.getHeight() + 2));

	}
}
