/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.settings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/// A custom text field, not a vanilla `EditBox`, so Controlify can't open its on-screen keyboard for it by itself.
public class NameField extends AbstractWidget {
	private static final int MAX_LENGTH = 24;

	public NameField(int x, int y, int width, int height) {
		super(x, y, width, height, Component.translatable("examplemod.settings.name"));
	}

	public void insert(String text) {
		String name = BoatSettings.boatName + text;
		BoatSettings.boatName = name.length() > MAX_LENGTH ? name.substring(0, MAX_LENGTH) : name;
	}

	public void deleteLast() {
		String name = BoatSettings.boatName;
		if (!name.isEmpty()) {
			BoatSettings.boatName = name.substring(0, name.length() - 1);
		}
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (!event.isAllowedChatCharacter()) {
			return false;
		}
		this.insert(event.codepointAsString());
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == InputConstants.KEY_BACKSPACE) {
			this.deleteLast();
			return true;
		}
		return false;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics/*? if <26.4 {*//*, int mouseX, int mouseY, float tickDelta*//*?}*/) {
		graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0xFF000000);
		graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0);
		String text = BoatSettings.boatName + (this.isFocused() ? "_" : "");
		graphics.text(Minecraft.getInstance().font, text, this.getX() + 4, this.getY() + (this.getHeight() - 8) / 2, 0xFFE0E0E0);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, this.getMessage());
	}
}
