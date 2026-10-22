/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.controllers;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.impl.gui.screen.BindConsumerScreen;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class BindController implements Controller<Optional<LogicalInput>> {
	private final Option<Optional<LogicalInput>> option;
	public final ControllerImpl controller;
	private boolean conflicting;

	public BindController(Option<Optional<LogicalInput>> option, ControllerImpl controller) {
		this.option = option;
		this.controller = controller;
	}

	@Override
	public Option<Optional<LogicalInput>> option() {
		return this.option;
	}

	@Override
	public Component formatValue() {
		return Component.empty();
	}

	public void setConflicting(boolean conflicting) {
		this.conflicting = conflicting;
	}

	public boolean getConflicting() {
		return this.conflicting;
	}

	@Override
	public BindControllerElement provideWidget(YACLScreen yaclScreen, Dimension<Integer> dimension) {
		return new BindControllerElement(this, yaclScreen, dimension);
	}

	public static class BindControllerElement extends ControllerWidget<BindController> implements ElementBehaviour {
		public boolean awaitingControllerInput = false;
		private final Component awaitingText = Component.translatable("controlify.gui.bind_input_awaiting").withStyle(ChatFormatting.ITALIC);

		public BindControllerElement(BindController control, YACLScreen screen, Dimension<Integer> dim) {
			super(control, screen, dim);
		}

		@Override
		protected void extractValueText(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			if (awaitingControllerInput) {
				graphics.text(textRenderer, awaitingText, getDimension().xLimit() - textRenderer.width(awaitingText) - getXPadding(), (int) (getDimension().centerY() - textRenderer.lineHeight / 2f), 0xFFFFFFFF, true);
			} else {
				var bind = control.option().pendingValue();
				if (bind == null) {
					return;
				}

				Component text = Controlify.instance().inputFontMapper()
						.getComponentFromInput((control.controller == null ? ControllerType.DEFAULT.id() : control.controller.info().type().id()), bind.orElse(null));
				int width = textRenderer.width(text);

				graphics.text(textRenderer, text, getDimension().xLimit() - width - 1, (int) (getDimension().centerY() - textRenderer.lineHeight / 2f + 1), -1, false);
			}
		}

		@Override
		public boolean keyPressed(KeyEvent keyEvent) {
			if (isFocused() && keyEvent.key() == InputConstants.KEY_RETURN) {
				openConsumerScreen();
				return true;
			}

			return false;
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
			if (getDimension().isPointInside((int) mouseButtonEvent.x(), (int) mouseButtonEvent.y())) {
				openConsumerScreen();
				return true;
			}

			return false;
		}

		private void openConsumerScreen() {
			if (control.controller == null) {
				return;
			}
			awaitingControllerInput = true;
			MinecraftUtil.setScreen(new BindConsumerScreen(control.controller, control.option(), this, MinecraftUtil.getScreen()));
		}

		@Override
		public void handleInteraction(ScreenContext<?> screen, dev.isxander.controlify.api.controller.Controller controller, Runnable next) {
			if (controller != control.controller) {
				return;
			}

			if (ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses()) {
				openConsumerScreen();
				return;
			}

			next.run();
		}

		@Override
		protected int getHoveredControlWidth() {
			return getUnhoveredControlWidth();
		}

		@Override
		protected int getUnhoveredControlWidth() {
			if (awaitingControllerInput) {
				return textRenderer.width(awaitingText);
			}

			Component text = Controlify.instance().inputFontMapper()
					.getComponentFromInput((control.controller == null ? ControllerType.DEFAULT.id() : control.controller.info().type().id()), control.option().pendingValue().orElse(null));
			return textRenderer.width(text);
		}

		@Override
		protected int getValueColor() {
			return control.conflicting ? 0xFFFF5555 : super.getValueColor();
		}

	}
}
