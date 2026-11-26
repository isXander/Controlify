/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.virtualmouse;

import dev.isxander.controlify.api.screenop.SnapPoint;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookPageAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;
import java.util.function.Consumer;

public final class SnapUtils {
	private SnapUtils() {
	}

	public static void addRecipeSnapPoints(RecipeBookComponent<?> recipeBookComponent, Consumer<SnapPoint> consumer) {
		if (recipeBookComponent.isVisible()) {
			RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
			getVisibleRecipeTabs(componentAccessor).forEach(button -> {
				int x = button.getX() + button.getWidth() / 2;
				int y = button.getY() + button.getHeight() / 2;
				consumer.accept(new SnapPoint(new Vector2i(x, y), 20));
			});

			var filterButton = componentAccessor.controlify$getFilterButton();
			if (filterButton./*? if >=26.4 {*/isVisible()/*?} else {*//*visible*//*?}*/) {
				int x = filterButton.getX() + filterButton.getWidth() / 2;
				int y = filterButton.getY() + filterButton.getHeight() / 2;
				consumer.accept(new SnapPoint(new Vector2i(x, y), 14));
			}

			RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.controlify$getRecipeBookPage();
			pageAccessor.controlify$getButtons().forEach(button -> {
				int x = button.getX() + button.getWidth() / 2;
				int y = button.getY() + button.getHeight() / 2;
				consumer.accept(new SnapPoint(new Vector2i(x, y), 21));
			});

			var forwardButton = pageAccessor.controlify$getForwardButton();
			if (forwardButton./*? if >=26.4 {*/isVisible()/*?} else {*//*visible*//*?}*/) {
				int x = forwardButton.getX() + forwardButton.getWidth() / 2 - 2;
				int y = forwardButton.getY() + forwardButton.getHeight() / 2;
				consumer.accept(new SnapPoint(new Vector2i(x, y), 10));
			}

			var backButton = pageAccessor.controlify$getBackButton();
			if (backButton./*? if >=26.4 {*/isVisible()/*?} else {*//*visible*//*?}*/) {
				int x = backButton.getX() + backButton.getWidth() / 2 + 2;
				int y = backButton.getY() + backButton.getHeight() / 2;
				consumer.accept(new SnapPoint(new Vector2i(x, y), 10));
			}
		}
	}

	/// The recipe book's category tabs that are shown, top to bottom.
	public static List<? extends AbstractWidget> getVisibleRecipeTabs(RecipeBookComponentAccessor accessor) {
		//? if >=26.4 {
		return accessor.controlify$getTabBar().children().stream()
				.filter(AbstractWidget.class::isInstance)
				.map(AbstractWidget.class::cast)
				.filter(AbstractWidget::isVisible)
				.toList();
		//?} else {
		//return accessor.controlify$getTabButtons().stream().filter(tab -> tab.visible).toList();
		//?}
	}

	public static @Nullable AbstractWidget getSelectedRecipeTab(RecipeBookComponentAccessor accessor) {
		//? if >=26.4 {
		return accessor.controlify$getTabBar().currentTabButton();
		//?} else {
		//return accessor.controlify$getSelectedTab();
		//?}
	}
}
