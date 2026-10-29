/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat.yacl;

import dev.isxander.controlify.compat.CompatMixinPlugin;

public class YACLMixinPlugin extends CompatMixinPlugin {
	@Override
	protected String getModId() {
		return "yet_another_config_lib_v3";
	}
}
