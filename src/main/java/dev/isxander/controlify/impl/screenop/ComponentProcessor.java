/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.keyboard.ComponentKeyboardBehaviour;

public interface ComponentProcessor extends ComponentProcessorProvider {
	ComponentProcessor EMPTY = new ComponentProcessor(){};

	default boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerImpl controller) {
		return false;
	}

	default boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerImpl controller) {
		return false;
	}

	default void onFocusGained(ScreenProcessor<?> screen, ControllerImpl controller) {
	}

	default boolean shouldKeepFocusOnKeyboardMode(ScreenProcessor<?> screen) {
		return false;
	}

	default ComponentKeyboardBehaviour getKeyboardBehaviour(ScreenProcessor<?> screen, ControllerImpl controller) {
		return ComponentKeyboardBehaviour.UNDEFINED;
	}

	@Override
	default ComponentProcessor componentProcessor() {
		return this;
	}
}
