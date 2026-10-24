/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium.mixins;

//? if sodium {

import dev.isxander.controlify.impl.screenop.behaviours.element.ControlValueAccess;
import net.caffeinemc.mods.sodium.client.config.structure.IntegerOption;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.options.control.AbstractOptionList;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl$SliderControlElement")
public abstract class SliderControlElementMixin extends ControlElement implements ControlValueAccess {
	@Shadow
	@Final
	private IntegerOption option;

	public SliderControlElementMixin(AbstractOptionList list, Dim2i dim, ColorTheme theme) {
		super(list, dim, theme);
	}

	@Override
	public void controlify$adjustValue(int direction) {
		var range = this.option.getSteppedValidator();
		this.option.modifyValue(Mth.clamp(
				this.option.getValidatedValue() + (direction < 0 ? -range.step() : range.step()),
				range.min(), range.max()
		));
	}
}
//?}
