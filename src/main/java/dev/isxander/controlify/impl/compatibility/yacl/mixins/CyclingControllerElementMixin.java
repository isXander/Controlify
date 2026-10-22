/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.yacl.mixins;

import dev.isxander.controlify.api.screenop.*;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingControllerElement;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CyclingControllerElement.class)
public class CyclingControllerElementMixin implements ElementBehaviourFactory<GuiEventListener> {

	@Override
	public ElementBehaviour create(GuiEventListener element) {
		return ElementBehaviours.conditional(
				() -> ((dev.isxander.controlify.impl.compatibility.yacl.mixins.ControllerWidgetAccessor) this).getControl().option().available(),
				ElementBehaviours.cycle((CyclingControllerElement widget, int direction) -> widget.cycleValue(direction)).create((CyclingControllerElement) (Object) this)
		);
	}

}
