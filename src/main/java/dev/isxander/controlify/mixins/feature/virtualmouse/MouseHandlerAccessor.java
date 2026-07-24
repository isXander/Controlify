/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.virtualmouse;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
	@Invoker("onMove")
	void controlify$invokeOnMove(long window, double x, double y);

	@Invoker("onButton")
	void controlify$invokeOnButton(long l, net.minecraft.client.input.MouseButtonInfo mouseButtonInfo, int i);

	@Invoker("onScroll")
	void controlify$invokeOnScroll(long window, double scrollDeltaX, double scrollDeltaY);

	@Accessor("mouseGrabbed")
	void controlify$setMouseGrabbed(boolean mouseGrabbed);
}
