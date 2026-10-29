/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.gui.ButtonGuideApi;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public final class ButtonGuideApiImpl implements ButtonGuideApi {
	public static final ButtonGuideApi INSTANCE = new ButtonGuideApiImpl();

	private ButtonGuideApiImpl() {
	}

	@Override
	public <T> void addGuideToButton(@NonNull T button, @NonNull ActionAccessor action, @NonNull ButtonGuidePredicate<T> renderPredicate) {
		addGuideToButton(button, () -> action, renderPredicate);
	}

	@Override
	public <T> void addGuideToButton(@NonNull T button, @NonNull Supplier<ActionAccessor> action, @NonNull ButtonGuidePredicate<T> renderPredicate) {
		ButtonGuideRenderer.registerActionForButton(button, action, renderPredicate);
	}
}
