/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.elements;

import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.AbstractButtonComponentProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractButton.class)
public class AbstractButtonMixin implements ComponentProcessorProvider {
	@Unique private final AbstractButtonComponentProcessor controlify$processor
			= new AbstractButtonComponentProcessor((AbstractButton) (Object) this);

	@Override
	public ComponentProcessor componentProcessor() {
		return controlify$processor;
	}
}
