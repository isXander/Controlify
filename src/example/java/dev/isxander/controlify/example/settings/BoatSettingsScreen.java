/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.settings;

import dev.isxander.controlify.example.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.Nullable;

/// The boat settings screen. It knows nothing about Controlify.
public class BoatSettingsScreen extends Screen {
	private final @Nullable Screen parent;

	public @Nullable Button resetButton;

	public BoatSettingsScreen(@Nullable Screen parent) {
		super(Component.translatable("examplemod.settings.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = this.width / 2 - 100;
		this.addRenderableWidget(new StringWidget(x, 40, 200, 12, Component.translatable("examplemod.settings.name"), this.font));
		this.addRenderableWidget(new NameField(x, 54, 200, 20));
		this.addRenderableWidget(new StringWidget(x, 84, 200, 12, Component.translatable("examplemod.settings.sensitivity"), this.font));
		this.addRenderableWidget(new SensitivityBar(x, 98, 200, 12));

		this.resetButton = this.addRenderableWidget(Button.builder(
				Component.translatable("examplemod.settings.reset"), button -> this.reset()
		).bounds(x, this.height - 52, 98, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
				.bounds(x + 102, this.height - 52, 98, 20).build());
	}

	public void reset() {
		BoatSettings.reset();
	}

	@Override
	public void onClose() {
		Screens.set(this.parent);
	}
}
