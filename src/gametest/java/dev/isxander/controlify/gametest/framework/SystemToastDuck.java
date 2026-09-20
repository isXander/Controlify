/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework;

import net.minecraft.network.chat.Component;

public interface SystemToastDuck {
	Component controlify_test$getTitle();

	Component controlify_test$getMessage();
}
