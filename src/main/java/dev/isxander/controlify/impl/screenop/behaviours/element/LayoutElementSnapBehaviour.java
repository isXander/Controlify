/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.element;

import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.SnapPoint;
import net.minecraft.client.gui.layouts.LayoutElement;

import java.util.function.Consumer;

/// A centre target derived solely from layout geometry. Registration determines
/// which interactive element types use this behaviour.
public final class LayoutElementSnapBehaviour implements ElementBehaviour {
	private final LayoutElement element;

	public LayoutElementSnapBehaviour(LayoutElement element) {
		this.element = element;
	}

	@Override
	public void collectSnapPoints(ScreenContext<?> screen, Consumer<SnapPoint> output, Runnable next) {
		output.accept(new SnapPoint(
				element.getX() + element.getWidth() / 2,
				element.getY() + element.getHeight() / 2,
				Math.min(element.getWidth(), element.getHeight()) / 2 + 10
		));
		next.run();
	}
}
