/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.rumble.effects;

import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import org.jetbrains.annotations.Nullable;

public interface UseItemEffectHolder {
	@Nullable ContinuousRumbleEffect controlify$getUseItemEffect();
}
