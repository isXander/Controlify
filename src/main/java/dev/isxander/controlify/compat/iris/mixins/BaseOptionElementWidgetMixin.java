/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat.iris.mixins;

//? if iris {

import dev.isxander.controlify.impl.screenop.behaviours.element.ControlValueAccess;
import net.irisshaders.iris.gui.NavigationController;
import net.irisshaders.iris.gui.element.widget.BaseOptionElementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BaseOptionElementWidget.class)
public abstract class BaseOptionElementWidgetMixin implements ControlValueAccess {
	@Shadow
	public abstract boolean applyPreviousValue();

	@Shadow
	public abstract boolean applyNextValue();

	@Shadow
	protected NavigationController navigation;

	@Override
	public void controlify$adjustValue(int direction) {
		boolean needsUpdate = direction < 0 ? this.applyPreviousValue() : this.applyNextValue();
		if (needsUpdate) {
			this.navigation.refresh();
		}
	}
}
//?}
