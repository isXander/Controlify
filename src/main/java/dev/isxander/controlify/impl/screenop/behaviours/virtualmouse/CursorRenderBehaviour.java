/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.virtualmouse;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.debug.DebugProperties;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.impl.screenop.VirtualMouseState;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class CursorRenderBehaviour implements ScreenBehaviour {
	private static final Identifier CURSOR_TEXTURE = CUtil.rl("textures/gui/virtual_mouse.png");

	private final ScreenContextImpl<?> context;
	private final VirtualMouseState mouse;

	public CursorRenderBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
		this.mouse = context.virtualMouse();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller controller, Runnable next) {
		next.run();
		if (!context.isCurrentScreen() || !mouse.isEnabled()) {
			return;
		}
		if (DebugProperties.DEBUG_SNAPPING) {
			for (var snapPoint : mouse.snapPoints()) {
				graphics.fill(snapPoint.position().x() - snapPoint.range(), snapPoint.position().y() - snapPoint.range(), snapPoint.position().x() + snapPoint.range(), snapPoint.position().y() + snapPoint.range(), 0x33FFFFFF);
				graphics.fill(snapPoint.position().x() - 1, snapPoint.position().y() - 1, snapPoint.position().x() + 1, snapPoint.position().y() + 1, snapPoint.equals(mouse.lastSnappedPoint()) ? 0xFFFFFF00 : 0xFFFF0000);
			}
		}

		var scaledX = mouse.x() * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth();
		var scaledY = mouse.y() * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight();

		var pose = graphics.pose().pushMatrix();
		pose.translate((float) scaledX, (float) scaledY);
		graphics.nextStratum();
		pose.scale(0.5f, 0.5f);

		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				CURSOR_TEXTURE,
				-16, -16,
				0, 0,
				32, 32,
				32, 32
		);

		pose.popMatrix();
	}
}
