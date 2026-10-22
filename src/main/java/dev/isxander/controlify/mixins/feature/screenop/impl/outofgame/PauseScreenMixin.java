/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.PauseScreenBehaviour;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PauseScreen.class)
public class PauseScreenMixin implements ScreenBehaviourFactory<PauseScreen> {
	@Shadow
	private @Nullable Button disconnectButton;

	@Override
	public ScreenBehaviour create(@NonNull ScreenContext<? extends PauseScreen> context) {
		return new PauseScreenBehaviour(context, () -> disconnectButton);
	}
}
