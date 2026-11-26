/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.impl.virtualmouse.SnapUtils;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookPageAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.util.List;
import java.util.function.Consumer;

public final class RecipeBookScreenBehaviour implements ScreenBehaviour {

	private final RecipeBookScreenAccessor recipeBookScreenAccessor;

	private final ScreenContext<? extends AbstractRecipeBookScreen<?>> context;

	public RecipeBookScreenBehaviour(ScreenContext<? extends AbstractRecipeBookScreen<?>> context) {
		this.context = context;
		this.recipeBookScreenAccessor = (RecipeBookScreenAccessor) context.screen();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (!context.isVirtualMouseEnabled()) {
			next.run();
			return;
		}

		RecipeBookComponent<?> recipeBookComponent = recipeBookScreenAccessor.controlify$getRecipeBookComponent();

		if (!recipeBookComponent.isVisible()) {
			next.run();
			return;

		}
		for (var action : List.of(ControlifyActions.VMOUSE_PAGE_NEXT, ControlifyActions.VMOUSE_PAGE_PREV,
				ControlifyActions.VMOUSE_PAGE_DOWN, ControlifyActions.VMOUSE_PAGE_UP)) {
			while (action.on(controller).pulse().consumePulse()) {
				RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
				RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.controlify$getRecipeBookPage();
				List<? extends AbstractWidget> tabs = SnapUtils.getVisibleRecipeTabs(componentAccessor);
				int index = tabs.indexOf(SnapUtils.getSelectedRecipeTab(componentAccessor));
				AbstractWidget button = null;
				if (action == ControlifyActions.VMOUSE_PAGE_NEXT) {
					button = pageAccessor.controlify$getForwardButton();
				} else if (action == ControlifyActions.VMOUSE_PAGE_PREV) {
					button = pageAccessor.controlify$getBackButton();
				} else if (action == ControlifyActions.VMOUSE_PAGE_DOWN && index >= 0 && index + 1 < tabs.size()) {
					button = tabs.get(index + 1);
				} else if (action == ControlifyActions.VMOUSE_PAGE_UP && index > 0) {
					button = tabs.get(index - 1);
				}
				if (button != null) {
					recipeBookComponent.mouseClicked(new MouseButtonEvent(
							button.getX(), button.getY(),
							new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)), false);
				}
			}
		}
		next.run();
	}

	@Override
	public void collectSnapPoints(Consumer<SnapPoint> output, Runnable next) {
		SnapUtils.addRecipeSnapPoints(
				recipeBookScreenAccessor.controlify$getRecipeBookComponent(), output);
		next.run();
	}

	public interface RecipeBookScreenAccessor {
		RecipeBookComponent<?> controlify$getRecipeBookComponent();
	}
}
