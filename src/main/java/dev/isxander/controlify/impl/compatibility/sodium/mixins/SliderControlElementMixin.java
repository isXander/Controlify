/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium.mixins;

//? if sodium {

import dev.isxander.controlify.api.screenop.*;
import net.caffeinemc.mods.sodium.client.config.structure.IntegerOption;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.options.control.AbstractOptionList;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl$SliderControlElement")
public abstract class SliderControlElementMixin extends ControlElement implements ElementBehaviourFactory<GuiEventListener> {
	@Shadow
	@Final
	private IntegerOption option;

	public SliderControlElementMixin(AbstractOptionList list, Dim2i dim, ColorTheme theme) {
		super(list, dim, theme);
	}

	@Override
	public ElementBehaviour create(GuiEventListener element) {
		return ElementBehaviours.slider((GuiEventListener widget, int direction) -> this.controlify$incrementSlider(direction < 0)).create((GuiEventListener) (Object) this);
	}

	@Unique
	private void controlify$incrementSlider(boolean reverse) {
		var range = option.getSteppedValidator();
		option.modifyValue(Mth.clamp(
				option.getValidatedValue() + (reverse ? -range.step() : range.step()),
				range.min(), range.max()
		));
	}
}
//?}
