/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.iris.mixins;

//? if iris {

import dev.isxander.controlify.api.screenop.*;
import net.irisshaders.iris.gui.NavigationController;
import net.irisshaders.iris.gui.element.widget.BaseOptionElementWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BaseOptionElementWidget.class)
public abstract class BaseOptionElementWidgetMixin implements ElementBehaviourFactory<GuiEventListener> {
	@Shadow
	public abstract boolean applyPreviousValue();

	@Shadow
	public abstract boolean applyNextValue();

	@Shadow
	protected NavigationController navigation;

	@Override
	public ElementBehaviour create(GuiEventListener element) {
		return ElementBehaviours.slider((GuiEventListener widget, int direction) -> this.cycle(direction < 0)).create((GuiEventListener) (Object) this);
	}

	@Unique
	private void cycle(boolean reverse) {
		boolean needsUpdate = reverse ? applyPreviousValue() : applyNextValue();
		if (needsUpdate) {
			navigation.refresh();
		}
	}

}
//?}
