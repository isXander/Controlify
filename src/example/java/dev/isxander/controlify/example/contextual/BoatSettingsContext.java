/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.contextual;

import dev.isxander.controlify.api.contextual.Context;
import dev.isxander.controlify.api.contextual.GuideVerbosity;
import dev.isxander.controlify.api.controller.Controller;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jspecify.annotations.Nullable;

// #region context
/// What the boat settings domain describes: the controller, and the focused element.
public record BoatSettingsContext(Controller controller, @Nullable GuiEventListener focused) implements Context {
	@Override
	public GuideVerbosity verbosity() {
		return this.controller.guideVerbosity();
	}
}
// #endregion context
