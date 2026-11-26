/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=26.4 {
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
//?} else {
/*import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;

import java.util.List;
*///?}

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {
	@Accessor("recipeBookPage")
	RecipeBookPage controlify$getRecipeBookPage();

	//? if >=26.4 {
	@Accessor("tabBar")
	TabNavigationBar controlify$getTabBar();
	//?} else {
	/*@Accessor("tabButtons")
	List<RecipeBookTabButton> controlify$getTabButtons();

	@Accessor("selectedTab")
	RecipeBookTabButton controlify$getSelectedTab();
	*///?}

	@Accessor("filterButton")
	CycleButton<?> controlify$getFilterButton();
}
