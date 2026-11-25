/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.screen;

import dev.isxander.controlify.api.contextual.GuideInstance;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.example.contextual.BoatSettingsContext;
import dev.isxander.controlify.example.contextual.BoatSettingsDomain;
import dev.isxander.controlify.example.settings.BoatSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

// #region guide-behaviour
/// Shows the boat settings domain's button guide on the boat settings screen.
public class BoatSettingsGuideBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends BoatSettingsScreen> context;
	private final GuideInstance<BoatSettingsContext> guide =
			BoatSettingsDomain.DOMAIN.createGuideInstance(Minecraft.getInstance().font);

	public BoatSettingsGuideBehaviour(ScreenContext<? extends BoatSettingsScreen> context) {
		this.context = context;
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		List<GuiEventListener> focusPath = this.context.focusPath();
		this.guide.update(new BoatSettingsContext(controller, focusPath.isEmpty() ? null : focusPath.getFirst()));
		next.run();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller controller, Runnable next) {
		next.run();
		this.guide.extractRenderState(graphics, true, false);
	}
}
// #endregion guide-behaviour
