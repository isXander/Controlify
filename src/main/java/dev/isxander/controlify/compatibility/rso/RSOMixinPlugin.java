/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compatibility.rso;

import dev.isxander.controlify.compatibility.CompatMixinPlugin;

public class RSOMixinPlugin extends CompatMixinPlugin {
	@Override
	protected String getModId() {
		return "reeses-sodium-options";
	}
}
