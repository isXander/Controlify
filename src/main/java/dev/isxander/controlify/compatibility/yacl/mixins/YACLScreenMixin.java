/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.compatibility.yacl.screenop.YACLScreenProcessor;
import dev.isxander.yacl3.gui.YACLScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(YACLScreen.class)
public class YACLScreenMixin implements ScreenProcessorProvider {
	@Unique private final YACLScreenProcessor controlify$processor = new YACLScreenProcessor((YACLScreen) (Object) this);

	@Override
	public ScreenProcessor<?> screenProcessor() {
		return controlify$processor;
	}
}
