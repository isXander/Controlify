/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.yacl.mixins;

import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.compatibility.yacl.screenop.StringControllerElementBehaviour;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StringControllerElement.class)
public class StringControllerElementMixin implements ElementBehaviourFactory<GuiEventListener> {

	@Override
	public ElementBehaviour create(GuiEventListener element) {
		return new StringControllerElementBehaviour((StringControllerElement) (Object) this);
	}

}
