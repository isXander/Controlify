/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {
	@Invoker("changeFocus")
	void controlify$invokeChangeFocus(ComponentPath path);

	@Invoker("clearFocus")
	void controlify$invokeClearFocus();

	@Invoker("setInitialFocus")
	void invokeSetInitialFocus();

	@Accessor("renderables")
	List<Renderable> controlify$getRenderables();
}
