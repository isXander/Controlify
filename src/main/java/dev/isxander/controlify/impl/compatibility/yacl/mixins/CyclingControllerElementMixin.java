/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.yacl.mixins;

import dev.isxander.controlify.impl.screenop.ComponentProcessor;
import dev.isxander.controlify.impl.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.impl.compatibility.yacl.screenop.CyclingControllerElementComponentProcessor;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingControllerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CyclingControllerElement.class)
public class CyclingControllerElementMixin implements ComponentProcessorProvider {
	@Unique private final CyclingControllerElementComponentProcessor controlify$processor
			= new CyclingControllerElementComponentProcessor((CyclingControllerElement) (Object) this);

	@Override
	public ComponentProcessor componentProcessor() {
		return controlify$processor;
	}
}
