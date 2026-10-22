/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.yacl.mixins;

import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.compatibility.yacl.screenop.YACLScreenBehaviour;
import dev.isxander.yacl3.gui.YACLScreen;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(YACLScreen.class)
public class YACLScreenMixin implements ScreenBehaviourFactory<YACLScreen> {
	@Override
	public ScreenBehaviour create(@NonNull ScreenContext<? extends YACLScreen> context) {
		return new YACLScreenBehaviour(context);
	}
}
