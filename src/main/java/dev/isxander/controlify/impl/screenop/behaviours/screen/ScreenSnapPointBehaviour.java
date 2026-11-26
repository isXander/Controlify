/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.screen;

import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;

import java.util.List;
import java.util.function.Consumer;

public final class ScreenSnapPointBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;

	public ScreenSnapPointBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
	}

	@Override
	public void collectSnapPoints(Consumer<SnapPoint> output, Runnable next) {
		for (var element : List.copyOf(context.screen().children())) {
			context.elementBehaviour(element).collectSnapPoints(context, output, () -> {});
		}
		next.run();
	}
}
