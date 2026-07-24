/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop;

import dev.isxander.controlify.screenop.CustomFocus;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin extends CustomFocus {
	@Shadow
	@Nullable GuiEventListener getFocused();

	@Override
	default GuiEventListener getCustomFocus() {
		return this.getFocused();
	}
}
