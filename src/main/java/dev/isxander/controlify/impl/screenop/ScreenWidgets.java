/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.function.Predicate;

/// Finds live widgets after a screen has rebuilt them, including nested layouts.
public final class ScreenWidgets {
	private ScreenWidgets() {
	}

	public static Optional<AbstractWidget> find(Screen screen, String translationKey) {
		return find(screen, widget -> widget.getMessage().getContents() instanceof TranslatableContents contents
									&& contents.getKey().equals(translationKey));
	}

	public static Optional<AbstractWidget> find(Screen screen, Component message) {
		return find(screen, widget -> widget.getMessage().equals(message));
	}

	private static Optional<AbstractWidget> find(Screen screen, Predicate<AbstractWidget> predicate) {
		var pending = new ArrayDeque<GuiEventListener>(screen.children());
		var visited = Collections.newSetFromMap(new IdentityHashMap<GuiEventListener, Boolean>());
		while (!pending.isEmpty()) {
			var element = pending.removeFirst();
			if (!visited.add(element)) {
				continue;
			}
			if (element instanceof AbstractWidget widget && predicate.test(widget)) {
				return Optional.of(widget);
			}
			if (element instanceof ContainerEventHandler container) {
				pending.addAll(container.children());
			}
		}
		return Optional.empty();
	}
}
