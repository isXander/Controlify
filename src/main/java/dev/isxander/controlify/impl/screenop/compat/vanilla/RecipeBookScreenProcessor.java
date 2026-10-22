/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.virtualmouse.VirtualMouseHandler;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookPageAccessor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RecipeBookScreenProcessor<T extends AbstractRecipeBookScreen<?>>
		extends AbstractContainerScreenProcessor<T> {

	private final RecipeBookScreenAccessor recipeBookScreenAccessor;

	public RecipeBookScreenProcessor(
			T screen,
			RecipeBookScreenAccessor recipeBookScreenAccessor,
			Supplier<Slot> hoveredSlot,
			ClickSlotFunction clickSlotFunction,
			Predicate<ControllerImpl> doItemSlotActions
	) {
		super(screen, hoveredSlot, clickSlotFunction, doItemSlotActions);
		this.recipeBookScreenAccessor = recipeBookScreenAccessor;
	}

	@Override
	protected void handleScreenVMouse(ControllerImpl controller, VirtualMouseHandler vmouse) {
		super.handleScreenVMouse(controller, vmouse);

		RecipeBookComponent<?> recipeBookComponent = recipeBookScreenAccessor.controlify$getRecipeBookComponent();

		if (!recipeBookComponent.isVisible()) {

			return;

		}
		RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
		RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.controlify$getRecipeBookPage();
		List<RecipeBookTabButton> tabs = componentAccessor
				.controlify$getTabButtons()
				.stream().filter(tab -> tab.visible)
				.toList();
		RecipeBookTabButton selectedTab = componentAccessor.controlify$getSelectedTab();

		ImageButton button = null;
		if (ControlifyActions.VMOUSE_PAGE_NEXT.on(controller).pulse().consumePulse()) {
			button = pageAccessor.controlify$getForwardButton();
		}
		if (ControlifyActions.VMOUSE_PAGE_PREV.on(controller).pulse().consumePulse()) {
			button = pageAccessor.controlify$getBackButton();
		}
		if (ControlifyActions.VMOUSE_PAGE_DOWN.on(controller).pulse().consumePulse()) {
			int index = tabs.indexOf(selectedTab);
			if (index != tabs.size() - 1) {
				button = tabs.get(index + 1);
			}
		}
		if (ControlifyActions.VMOUSE_PAGE_UP.on(controller).pulse().consumePulse()) {
			int index = tabs.indexOf(selectedTab);
			if (index != 0) {
				button = tabs.get(index - 1);
			}
		}
		if (button != null) {
			recipeBookComponent.mouseClicked(new MouseButtonEvent(
					button.getX(), button.getY(),
					new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)), false);
		}
	}

	public interface RecipeBookScreenAccessor {
		RecipeBookComponent<?> controlify$getRecipeBookComponent();
	}
}
