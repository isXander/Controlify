/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.rumble.effects;

import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import org.jetbrains.annotations.Nullable;

public interface UseItemEffectHolder {
	@Nullable RumbleEffectInstance controlify$getUseItemEffect();
}
