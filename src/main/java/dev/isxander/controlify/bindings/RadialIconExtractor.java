/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.RadialIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
		ItemStack stack = new ItemStack(Holder.direct(
			Items.STICK,
			DataComponentMap.builder()
				.set(DataComponents.MAX_STACK_SIZE, 1)
				.set(DataComponents.MAX_DAMAGE, 2)
				.set(DataComponents.DAMAGE, 0)
				.set(DataComponents.ITEM_MODEL, model)
				.build()
		));

		graphics.item(stack, x, y);
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
