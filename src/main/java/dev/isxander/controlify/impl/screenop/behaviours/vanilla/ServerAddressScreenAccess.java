/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import org.jspecify.annotations.Nullable;

public interface ServerAddressScreenAccess {
	EditBox controlify$getAddressField();

	@Nullable Button controlify$getDoneButton();

	@Nullable Button controlify$getBackButton();
}
