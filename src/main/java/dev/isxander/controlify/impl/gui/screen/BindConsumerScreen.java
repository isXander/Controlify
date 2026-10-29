/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.gui.controllers.BindController;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.impl.screenop.ScreenRuntime;
import dev.isxander.yacl3.api.utils.Dimension;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.Set;

public class BindConsumerScreen extends Screen {
	private final ControllerImpl controller;
	private EventSource.Subscription inputSubscription;
	private LogicalInput capturedInput;
	private final Set<LogicalInput> eligibleInputs = new HashSet<>();
	/// The binding currently in the slot being rebound.
	private final Optional<ActionBinding> current;
	private final Consumer<Optional<ActionBinding>> onResult;
	private final Screen backgroundScreen;
	/// Focus is lost while this screen covers the background, so it is put back on return.
	private final @Nullable ComponentPath backgroundFocus;
	private final BindController.BindControllerElement widgetToFocus;

	private int ticksTillClose;
	private int ticksTillInput;

	public BindConsumerScreen(
			ControllerImpl controller,
			Optional<ActionBinding> current,
			Consumer<Optional<ActionBinding>> onResult,
			BindController.BindControllerElement widgetToFocus,
			Screen backgroundScreen
	) {
		super(Component.empty());
		this.controller = controller;
		this.current = current;
		this.onResult = onResult;
		this.widgetToFocus = widgetToFocus;
		this.backgroundScreen = backgroundScreen;
		this.backgroundFocus = backgroundScreen.getCurrentFocusPath();
		this.ticksTillInput = 5;
	}

	@Override
	protected void init() {
		if (inputSubscription != null) {
			inputSubscription.close();
		}
		if (controller == null) {
			return;
		}
		inputSubscription = controller.input().inputPipeline().sourceLogicalSignals().subscribe(signal -> {
			if (ticksTillInput != 0 || ticksTillClose != 0 || capturedInput != null) {
				return;
			}
			if (signal instanceof LogicalSignal.ButtonPress press) {
				eligibleInputs.add(press.input());
			} else if (signal instanceof LogicalSignal.ButtonRelease release && eligibleInputs.remove(release.input())) {
				// Chords release before singles; releases also capture presses longer than a tap.
				capturedInput = release.input();
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
		Dimension<Integer> dim = widgetToFocus.awaitingSlotDimension();

		backgroundScreen.extractRenderStateWithTooltipAndSubtitles(graphics, dim.centerX(), dim.centerY(), a);

		graphics.nextStratum();

		// darken everything except the widget
		graphics.fill(0, 0, width, dim.y(), 0x80000000);
		graphics.fill(0, dim.y(), dim.x(), height, 0x80000000);
		graphics.fill(dim.xLimit(), dim.y(), width, height, 0x80000000);
		graphics.fill(dim.x(), dim.yLimit(), dim.xLimit(), height, 0x80000000);

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
				// Restore before the screen context reactivates, which would otherwise focus the first element.
				if (backgroundFocus != null && backgroundScreen.getFocused() == null) {
					backgroundFocus.applyFocus(true);
				}
				ScreenRuntime.onScreenChanged();
			}
		}

		if (ticksTillInput > 0) {
			ticksTillInput--;
			if (ticksTillInput > 0) {
				return;
			}
		}

		// tick runs after all controller input ticks

		if (capturedInput != null) {
			// Rebinding keeps the slot's gesture override.
			LogicalInput input = capturedInput;
			onResult.accept(Optional.of(current
					.map(binding -> new ActionBinding(input, binding.gesture()))
					.orElseGet(() -> ActionBinding.of(input))));
			capturedInput = null;
			returnToBackground();
		}
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent keyEvent) {
		if (ticksTillInput == 0 && ticksTillClose == 0
				&& (keyEvent.key() == InputConstants.KEY_BACKSPACE || keyEvent.key() == InputConstants.KEY_DELETE)) {
			onResult.accept(Optional.empty());
			returnToBackground();
			return true;
		}
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

	@Override
	public void onClose() {
		returnToBackground();
	}

	private void returnToBackground() {
		if (inputSubscription != null) {
			inputSubscription.close();
		}
		ticksTillClose = 5;
	}
}
