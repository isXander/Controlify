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
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

public final class DefaultWidgetBehaviour implements ElementBehaviour {
	private final AbstractWidget widget;

	public DefaultWidgetBehaviour(AbstractWidget widget) {
		this.widget = widget;
	}

	@Override
	public void collectSnapPoints(ScreenContext<?> screen, Consumer<SnapPoint> output, Runnable next) {
		if (widget.visible && widget.active) {
			next.run();
		}
	}
}
