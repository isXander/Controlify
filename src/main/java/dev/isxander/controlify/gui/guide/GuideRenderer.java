/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gui.guide;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NonNull;

public final class GuideRenderer {
	private GuideRenderer() {}

	public static void extractRenderState(GuiGraphicsExtractor graphics, GuideInstanceImpl<?> guideInstance, Minecraft minecraft, boolean bottomAligned, boolean textContrast, int guiScale) {
		Window window = minecraft.getWindow();
		int standardGuiScale = window.getGuiScale();
		if (guiScale == -1) { // unset
			guiScale = standardGuiScale;
		} else if (guiScale == 0) { // automatic
			int maxGuiScale = window.calculateScale(0, minecraft.isEnforceUnicode());
			// pick 1 less than the largest, but make sure it's not bigger than the standard scale
			guiScale = Math.min(maxGuiScale - 1, standardGuiScale);
		} else {
			guiScale = window.calculateScale(guiScale, minecraft.isEnforceUnicode());
		}

		graphics.pose().pushMatrix();
		int scaledWidth;
		int scaledHeight;
		if (guiScale > 0 && guiScale != standardGuiScale) {
			graphics.pose().scale((float) guiScale / standardGuiScale, (float) guiScale / standardGuiScale);
			scaledWidth = window.getWidth() / guiScale;
			scaledHeight = window.getHeight() / guiScale;
		} else {
			scaledWidth = graphics.guiWidth();
			scaledHeight = graphics.guiHeight();
		}

		extractLines(graphics, guideInstance.leftGuides(), minecraft.font, scaledWidth, scaledHeight, bottomAligned, false, textContrast);
		extractLines(graphics, guideInstance.rightGuides(), minecraft.font, scaledWidth, scaledHeight, bottomAligned, true, textContrast);

		graphics.pose().popMatrix();
	}

	private static void extractLines(GuiGraphicsExtractor graphics, PrecomputedLines lines, Font font, int width, int height, boolean bottomAligned, boolean rightAligned, boolean textContrast) {
		int safeAreaX = 2;
		int safeAreaY = 5;
		int betweenLines = 2;

		int allLinesHeight = lines.height() + (lines.lines().size() - 1) * betweenLines;

		int x = rightAligned ? (width - safeAreaX) : safeAreaX;
		int y = bottomAligned ? (height - allLinesHeight - safeAreaY) : safeAreaY;

		var list = bottomAligned ? Lists.reverse(lines.lines()) : lines.lines();
		for (PrecomputedLines.PrecomputedLine line : list) {
			int lineX = rightAligned ? (x - line.width()) : x;

			if (textContrast) {
				graphics.fill(
						lineX + line.backgroundLeft() - 1, y - 1,
						lineX + line.backgroundRight() + 1, y + font.lineHeight + 1, // use font.lineHeight for the height of the line since we're just contrasting the regular text
						0x80000000
				);
			}

			graphics.text(font, line.text(), lineX, y, 0xFFFFFFFF, !textContrast);

			y += line.height() + betweenLines;
		}
	}

	public static class Renderable implements net.minecraft.client.gui.components.Renderable {
		private final GuideInstanceImpl<?> instance;
		private final Minecraft minecraft;
		private boolean bottomAligned;
		private boolean textContrast;
		private int guiScale;

		public Renderable(GuideInstanceImpl<?> instance, Minecraft minecraft, boolean bottomAligned, boolean textContrast, int guiScale) {
			this.instance = instance;
			this.minecraft = minecraft;
			this.bottomAligned = bottomAligned;
			this.textContrast = textContrast;
			this.guiScale = guiScale;
		}


		@Override
		public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			GuideRenderer.extractRenderState(graphics, instance, minecraft, bottomAligned, textContrast, guiScale);
		}

		public void setBottomAligned(boolean bottomAligned) {
			this.bottomAligned = bottomAligned;
		}

		public void setTextContrast(boolean textContrast) {
			this.textContrast = textContrast;
		}

		public void setGuiScale(int guiScale) {
			this.guiScale = guiScale;
		}
	}
}
