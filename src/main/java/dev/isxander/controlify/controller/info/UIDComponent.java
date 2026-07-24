/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.info;

import dev.isxander.controlify.controller.SingleValueComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class UIDComponent extends SingleValueComponent<String> {
	public static final Identifier ID = CUtil.rl("uid");

	public UIDComponent(String value) {
		super(value, ID);
	}
}
