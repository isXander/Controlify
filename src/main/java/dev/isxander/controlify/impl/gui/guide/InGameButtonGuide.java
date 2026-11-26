/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.guide;

import dev.isxander.controlify.api.contextual.GuideInstance;
import dev.isxander.controlify.impl.config.settings.profile.GenericControllerSettings;
import dev.isxander.controlify.impl.contextual.ContextualDomains;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class InGameButtonGuide {
	private final ControllerImpl controller;
	private final Minecraft minecraft;
	private final GuideInstance<InGameContext> guideInstance;

	public InGameButtonGuide(ControllerImpl controller, Minecraft minecraft) {
		this.controller = controller;
		this.minecraft = minecraft;
		this.guideInstance = ContextualDomains.INSTANCE.inGame().createGuideInstance(minecraft.font);
	}

	public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta) {
		boolean debugOpen = minecraft.getDebugOverlay().showDebugScreen();
		//? if >=26.2 {
		boolean hideGui = minecraft.gui.hud.isHidden();
		//?} else {
		//boolean hideGui = minecraft.options.hideGui;
		//?}
		boolean screenOpen = MinecraftUtil.getScreen() != null;
		GenericControllerSettings.GuideSettings settings = controller.settings().generic.guide;

		if (!debugOpen && !hideGui && !screenOpen && settings.showIngameGuide) {
			this.guideInstance.extractRenderState(graphics, settings.ingameGuideBottom, true, settings.ingameGuiScale);
		}
	}

	public void tick() {
		GenericControllerSettings.GuideSettings settings = controller.settings().generic.guide;

		if (settings.showIngameGuide) {
			this.guideInstance.update(
					InGameContext.create(minecraft, controller)
			);
		}
	}
}
