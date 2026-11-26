/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.screen;

import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.SnapPoint;

import java.util.function.Consumer;

public final class AdditionalSnapPointsBehaviour implements ScreenBehaviour {
	private final Access access;

	public AdditionalSnapPointsBehaviour(ScreenContext<?> context) {
		this.access = (Access) context.screen();
	}

	@Override
	public void collectSnapPoints(Consumer<SnapPoint> consumer, Runnable next) {
		next.run();
		this.access.controlify$collectSnapPoints(consumer);
	}

	public interface Access {
		void controlify$collectSnapPoints(Consumer<SnapPoint> consumer);
	}
}
