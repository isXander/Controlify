/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import dev.isxander.controlify.impl.screenop.behaviours.vanilla.LanguageSelectionBehaviour;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screens.options.LanguageSelectScreen$LanguageSelectionList$Entry")
public class LanguageSelectionListEntryMixin implements LanguageSelectionBehaviour.Access {
	@Shadow
	@Final
	String code;

	@Override
	public String controlify$getLanguageCode() {
		return this.code;
	}
}
