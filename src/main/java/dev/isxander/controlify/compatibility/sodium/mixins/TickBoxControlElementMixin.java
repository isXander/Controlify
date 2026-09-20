/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compatibility.sodium.mixins;

//? if sodium {

import dev.isxander.controlify.compatibility.sodium.screenop.TickBoxControlProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.TickBoxControl$TickBoxControlElement")
public abstract class TickBoxControlElementMixin implements ComponentProcessorProvider {
	@Shadow
	protected abstract void toggleControl();

	@Unique private final ComponentProcessor controlify$componentProcessor
			= new TickBoxControlProcessor(this::toggleControl);

	@Override
	public ComponentProcessor componentProcessor() {
		return controlify$componentProcessor;
	}
}
//?}
