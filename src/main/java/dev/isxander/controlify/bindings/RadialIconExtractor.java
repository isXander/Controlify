/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.mixins.feature.bind.GuiGraphicsExtractorAccessor;
import dev.isxander.controlify.mixins.feature.bind.ItemStackRenderStateAccessor;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

public final class RadialIconExtractor {
	public static void extract(GuiGraphicsExtractor graphics, RadialIcon icon, int x, int y) {
		switch (icon.content()) {
			case RadialIcon.Empty _ -> {}
			case RadialIcon.Model(Identifier model) -> extractModel(graphics, model, x, y);
			case RadialIcon.Texture(Identifier texture) -> extractTexture(graphics, texture, x, y);
		}

		if (icon.overlay() != null) {
			graphics.text(Minecraft.getInstance().font, icon.overlay(), x, y, -1);
		}
	}

	private static void extractModel(GuiGraphicsExtractor graphics, Identifier model, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
		ItemDisplayContext displayContext = ItemDisplayContext.GUI;

		((ItemStackRenderStateAccessor) renderState).controlify$setDisplayContext(displayContext);
		renderState.setOversizedInGui(minecraft.getModelManager().getItemProperties(model).oversizedInGui());
		minecraft.getModelManager().getItemModel(model).update(
				renderState,
				ItemStack.EMPTY,
				minecraft.getItemModelResolver(),
				displayContext,
				minecraft.level,
				minecraft.player,
				0
		);

		if (!renderState.isEmpty()) {
			((GuiGraphicsExtractorAccessor) graphics).controlify$getGuiRenderState().addItem(
					new GuiItemRenderState(
							new Matrix3x2f(graphics.pose()),
							renderState,
							x,
							y,
							PlatformClientUtil.peekScissorStack(graphics)
					)
			);
		}
	}

	private static void extractTexture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y) {
		var pose = graphics.pose().pushMatrix();
		pose.translate(x, y);
		pose.scale(0.88f, 0.88f);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 18, 18);
		pose.popMatrix();
	}

	private RadialIconExtractor() {
	}
}
