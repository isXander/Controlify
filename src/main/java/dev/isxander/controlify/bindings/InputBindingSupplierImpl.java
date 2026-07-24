/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record InputBindingSupplierImpl(Identifier bindId) implements InputBindingSupplier {
	@Override
	public @Nullable InputBinding onOrNull(ControllerEntity controller) {
		return controller.input().map(c -> c.getBinding(bindId)).orElse(null);
	}

	@Override
	public Component inputGlyph() {
		return BindingFontHelper.binding(this.bindId());
	}
}
