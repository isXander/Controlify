/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.misc;

import dev.isxander.controlify.impl.controller.SingleValueControllerComponent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

public class UIDComponent extends SingleValueControllerComponent<String> {
	public static final Identifier ID = CUtil.rl("uid");

	public UIDComponent(String value) {
		super(ID, value);
	}
}
