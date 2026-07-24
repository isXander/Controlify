/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.screenop;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface ComponentProcessorProvider {
	ComponentProcessor componentProcessor();

	static ComponentProcessor provide(GuiEventListener component) {
		if (component == null) {
			return ComponentProcessor.EMPTY;
		}

		if (component instanceof ComponentProcessorProvider provider)
			return provider.componentProcessor();

		return REGISTRY.get(component).orElse(ComponentProcessor.EMPTY);

	}

	Registry<GuiEventListener, ComponentProcessor> REGISTRY = new Registry<>();
}
