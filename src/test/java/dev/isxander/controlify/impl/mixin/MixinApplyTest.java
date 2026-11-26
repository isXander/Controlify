/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.mixin;

import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.MixinEnvironment;

/// Applies every mixin up front, so a broken injector fails the build instead of the first time
/// its target class loads in game.
///
/// fabric-loader-junit runs the tests in the client environment with Controlify's mixin configs
/// loaded, but mixins normally only apply when their target class is loaded. The audit loads
/// every target, so a missing method, wrong descriptor, missing shadow or unmatched `require`
/// throws here. It does not check behaviour, and compat mixins for mods absent from the test
/// classpath are skipped by their config plugin.
class MixinApplyTest {
	@Test
	void allMixinsApply() {
		MixinEnvironment.getCurrentEnvironment().audit();
	}
}
