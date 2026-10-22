/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.keyboard;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import dev.isxander.controlify.api.gui.keyboard.KeyboardPositioner;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.mixins.feature.screenop.GuiMixin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;


/**
 * A screen with a single widget: the keyboard widget.
 * Renders this screen on top of the previous screen, and
 * quietly closes and returns to the previous screen without
 * reinitialisation when the keyboard is closed.
 * <p>
 * This is done to be minimally invasive to the underlying screen,
 * instead of adding and removing widgets, where focus to the keyboard could
 * be lost without the keyboard closing.
 *
 * The GUI mixin prevents calling removed() on the underlying screen when this
 * overlay is presented, since the underlying screen will be restored.
 */
public class KeyboardOverlayScreen extends Screen {
	private final Screen backgroundScreen;
	private final Identifier initialKeyboardLayout;
	private final InputTarget inputTarget;
	private final KeyboardPositioner keyboardPositioner;

	private KeyboardWidget keyboardWidget;

	public KeyboardOverlayScreen(
			Screen backgroundScreen,
			Identifier initialKeyboardLayout,
			InputTarget inputTarget,
			KeyboardPositioner keyboardPositioner
	) {
		super(backgroundScreen.getTitle());
		this.backgroundScreen = backgroundScreen;
		this.initialKeyboardLayout = initialKeyboardLayout;
		this.inputTarget = new WrappedInputTarget(inputTarget);
		this.keyboardPositioner = keyboardPositioner;
	}

	@Override
	protected void init() {
		KeyboardLayoutWithId layout = this.keyboardWidget != null
				? Controlify.instance().keyboardLayoutManager().getLayout(this.keyboardWidget.getCurrentLayoutId())
				: Controlify.instance().keyboardLayoutManager().getLayout(this.initialKeyboardLayout);

		ScreenRectangle keyboardRect = this.keyboardPositioner.positionKeyboard(this.width, this.height);

		this.keyboardWidget = this.addRenderableWidget(
				new KeyboardWidget(
						keyboardRect.left(),
						keyboardRect.top(),
						keyboardRect.width(),
						keyboardRect.height(),
						layout,
						this.inputTarget
				)
		);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		this.backgroundScreen.init(width, height);
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.backgroundScreen.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.nextStratum();
		super.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.backgroundScreen.extractBackground(graphics, mouseX, mouseY, a);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
			return true;
		} else {
			return this.backgroundScreen.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}
	}

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
		if (super.mouseClicked(mouseButtonEvent, doubleClick)) {
			return true;
		} else {
			if (this.backgroundScreen.mouseClicked(mouseButtonEvent, doubleClick)) {
				onClose();
				return true;
			}
		}
		return false;
	}

	@Override
	public void tick() {
		this.backgroundScreen.tick();
		super.tick();
	}

	@Override
	public void onClose() {
		// restore the previous screen without calling minecraft.setScreen
		// so the screen is not reinitialised
		MinecraftUtil.forceSetScreen(this.backgroundScreen);

		dev.isxander.controlify.impl.screenop.ScreenRuntime.onScreenChanged();
	}

	/**
	 * An input target that captures {@link InputConstants#KEY_RETURN} and {@link InputConstants#KEY_ESCAPE}
	 * key presses to close the keyboard overlay screen.
	 */
	private class WrappedInputTarget extends DelegatingInputTarget {
		public WrappedInputTarget(InputTarget target) {
			super(target);
		}

		@Override
		public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
			if (keycode == InputConstants.KEY_RETURN || keycode == InputConstants.KEY_ESCAPE) {
				// Close the keyboard overlay when pressing Enter or Escape
				KeyboardOverlayScreen.this.onClose();
				return true;
			}

			return super.acceptKeyCode(keycode, scancode, modifiers);
		}
	}

}
