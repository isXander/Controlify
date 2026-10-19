/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium;

import dev.isxander.controlify.impl.compatibility.CompatMixinPlugin;

public class SodiumMixinPlugin extends CompatMixinPlugin {
	@Override
	protected String getModId() {
		return "sodium";
	}
}
