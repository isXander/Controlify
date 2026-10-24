/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium.mixins;

//? if sodium {

import dev.isxander.controlify.impl.screenop.behaviours.element.ControlValueAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl$CyclingControlElement")
public abstract class CycleControlElementMixin implements ControlValueAccess {
	@Shadow
	protected abstract void cycleControl(boolean reverse);

	@Override
	public void controlify$adjustValue(int direction) {
		this.cycleControl(direction < 0);
	}
}
//?}
