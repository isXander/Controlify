/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compatibility.fancymenu;

import de.keksuccino.fancymenu.customization.action.ActionRegistry;

public final class FancyMenuCompat {
	public static void registerActions() {
		ActionRegistry.register(new OpenControlifySettingsAction());
	}
}
//?}
