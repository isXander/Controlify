/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.container;

import dev.isxander.controlify.impl.screenop.behaviours.vanilla.RecipeBookScreenBehaviour;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin
		implements RecipeBookScreenBehaviour.RecipeBookScreenAccessor {

	@Shadow
	@Final
	private RecipeBookComponent<?> recipeBookComponent;

	@Override
	public RecipeBookComponent<?> controlify$getRecipeBookComponent() {
		return this.recipeBookComponent;
	}

}
