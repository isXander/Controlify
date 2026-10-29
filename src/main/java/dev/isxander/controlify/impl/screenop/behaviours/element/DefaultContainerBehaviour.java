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
import net.minecraft.client.gui.components.events.ContainerEventHandler;

import java.util.List;
import java.util.function.Consumer;

public final class DefaultContainerBehaviour implements ElementBehaviour {
	private final ContainerEventHandler container;

	public DefaultContainerBehaviour(ContainerEventHandler container) {
		this.container = container;
	}

	@Override
	public void collectSnapPoints(ScreenContext<?> screen, Consumer<SnapPoint> output, Runnable next) {
		for (var child : List.copyOf(container.children())) {
			screen.elementBehaviour(child).collectSnapPoints(screen, output, () -> {});
		}
		// Child targets replace the generic centre target of the container itself.
	}
}
