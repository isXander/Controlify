/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.screen;

import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.gui.controllers.BindController;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class BindConsumerScreen extends Screen {
	private final ControllerImpl controller;
	private EventSource.Subscription inputSubscription;
	private LogicalInput capturedInput;
	private final Option<Optional<LogicalInput>> option;
	private final Screen backgroundScreen;
	private final BindController.BindControllerElement widgetToFocus;

	private int ticksTillClose;
	private int ticksTillInput;

	public BindConsumerScreen(ControllerImpl controller, Option<Optional<LogicalInput>> option, BindController.BindControllerElement widgetToFocus, Screen backgroundScreen) {
		super(Component.empty());
		this.controller = controller;
		this.option = option;
		this.widgetToFocus = widgetToFocus;
		this.backgroundScreen = backgroundScreen;
		this.ticksTillInput = 5;
	}

	@Override
	protected void init() {
		if (inputSubscription != null) {
			inputSubscription.close();
		}
		inputSubscription = controller.input().inputPipeline().sourceLogicalSignals().subscribe(signal -> {
			if (ticksTillInput == 0 && ticksTillClose == 0 && capturedInput == null
				&& signal instanceof LogicalSignal.Tapped tap) {
				capturedInput = tap.input();
			}
		});
	}

	@Override
	public void removed() {
		if (inputSubscription != null) {
			inputSubscription.close();
		}
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		Dimension<Integer> dim = widgetToFocus.getDimension();

		backgroundScreen.extractRenderStateWithTooltipAndSubtitles(graphics, dim.centerX(), dim.centerY(), a);

		graphics.nextStratum();

		// darken everything except the widget
		graphics.fill(0, 0, width, dim.y() - 1, 0x80000000);
		graphics.fill(0, dim.y(), dim.x() - 1, height, 0x80000000);
		graphics.fill(dim.xLimit() + 1, dim.y() - 1, width, height, 0x80000000);
		graphics.fill(dim.x(), dim.yLimit() + 1, dim.xLimit(), height, 0x80000000);

		super.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		// do not render background
	}

	@Override
	public void tick() {
		if (ticksTillClose > 0) {
			ticksTillClose--;
			if (ticksTillClose == 0) {
				widgetToFocus.awaitingControllerInput = false;
				// don't call setScreen because will cause background to re-init
				MinecraftUtil.forceSetScreen(backgroundScreen);
			}
		}

		if (ticksTillInput > 0) {
			ticksTillInput--;
			if (ticksTillInput > 0) {
				return;
			}
		}

		// tick runs after all controller input ticks

		Optional<LogicalInput> pressedBind = Optional.ofNullable(capturedInput);
		if (pressedBind.isPresent()) {
			option.requestSet(pressedBind);
			capturedInput = null;
			returnToBackground();
		}
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent keyEvent) {
		boolean consumed = super.keyPressed(keyEvent);
		if (consumed) {
			return true;
		}

		if (ticksTillInput > 0) {

			return false;

		}
		returnToBackground();
		return true;
	}

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
		boolean consumed = super.mouseClicked(mouseButtonEvent, doubleClick);
		if (consumed) {
			return true;
		}

		if (ticksTillInput > 0) {

			return false;

		}
		returnToBackground();
		return true;
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent mouseButtonEvent, double dx, double dy) {
		boolean consumed = super.mouseDragged(mouseButtonEvent, dx, dy);
		if (consumed) {
			return true;
		}

		if (ticksTillInput > 0) {

			return false;

		}
		returnToBackground();
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
		boolean consumed = super.mouseScrolled(mouseX, mouseY, amount, d);
		if (consumed) {
			return true;
		}

		if (ticksTillInput > 0) {

			return false;

		}
		returnToBackground();
		return true;
	}

	private void returnToBackground() {
		if (inputSubscription != null) {
			inputSubscription.close();
		}
		ticksTillClose = 5;
	}
}
