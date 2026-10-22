/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium.mixins;

//? if sodium {

import dev.isxander.controlify.api.screenop.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.TickBoxControl$TickBoxControlElement")
public abstract class TickBoxControlElementMixin implements ElementBehaviourFactory<GuiEventListener> {
	@Shadow
	protected abstract void toggleControl();

	@Override
	public ElementBehaviour create(GuiEventListener element) {
		return ElementBehaviours.cycle((GuiEventListener widget, int direction) -> this.toggleControl()).create((GuiEventListener) (Object) this);
	}

}
//?}
