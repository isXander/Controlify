/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.yacl.screenop.SliderControllerElementComponentProcessor;
import dev.isxander.yacl3.gui.controllers.slider.SliderControllerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SliderControllerElement.class)
public class SliderControllerElementMixin implements ComponentProcessorProvider {
	@Unique private final SliderControllerElementComponentProcessor controlify$processor
			= new SliderControllerElementComponentProcessor((SliderControllerElement) (Object) this);

	@Override
	public ComponentProcessor componentProcessor() {
		return controlify$processor;
	}
}
