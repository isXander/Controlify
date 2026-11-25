/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.events;

import dev.isxander.controlify.api.controllermanager.ControllerEvents;
import dev.isxander.controlify.api.utils.event.EventSource;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/// Shows the most recently connected controller while it is open.
public class ControllerStatusScreen extends Screen {
	private Component status = Component.literal("No controller connected yet");
	private @Nullable StringWidget statusWidget;

	public ControllerStatusScreen() {
		super(Component.literal("Controller status"));
	}

	// #region subscription
	private EventSource.@Nullable Subscription connectedSubscription;

	@Override
	public void added() {
		super.added();
		this.connectedSubscription = ControllerEvents.CONTROLLER_CONNECTED.subscribe(event ->
				this.setStatus(Component.literal("Connected " + event.controller().name())));
	}

	@Override
	public void removed() {
		super.removed();
		if (this.connectedSubscription != null) {
			this.connectedSubscription.close();
			this.connectedSubscription = null;
		}
	}
	// #endregion subscription

	@Override
	protected void init() {
		this.statusWidget = this.addRenderableWidget(new StringWidget(20, 20, this.status, this.font));
	}

	private void setStatus(Component status) {
		this.status = status;
		if (this.statusWidget != null) {
			this.statusWidget.setMessage(status);
		}
	}
}
