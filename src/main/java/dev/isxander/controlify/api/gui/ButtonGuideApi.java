/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.gui;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.impl.gui.ButtonGuideRenderer;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;

/**
 * Adds a guide to a button. This does not invoke the button press on binding trigger, only renders the guide.
 * This should be called every time a button is initialised, like in {@link Screen#init()}
 */
public final class ButtonGuideApi {

	private ButtonGuideApi() {
	}

	/**
	 * Makes the button render the image of the binding specified.
	 * This does not invoke the button press on binding trigger, only renders the guide.
	 * Custom behaviour should be handled inside a {@link dev.isxander.controlify.impl.screenop.ScreenProcessor} or {@link dev.isxander.controlify.impl.screenop.ComponentProcessor}
	 *
	 * @param button          button to render the guide for
	 * @param action          the action binding to render
	 * @param renderPredicate whether the guide should be rendered
	 */
	public static <T> void addGuideToButton(
			T button,
			ActionAccessor action,
			ButtonGuidePredicate<T> renderPredicate) {
		ButtonGuideRenderer.registerActionForButton(button, () -> action, renderPredicate);
	}

	public static <T> void addGuideToButton(
			T button,
			Supplier<ActionAccessor> action,
			ButtonGuidePredicate<T> renderPredicate) {
		ButtonGuideRenderer.registerActionForButton(button, action, renderPredicate);
	}
}
