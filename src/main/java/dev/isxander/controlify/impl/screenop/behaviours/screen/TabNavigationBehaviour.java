/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.screen;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.TabNavigationBarAccessor;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;

import java.util.Optional;

public final class TabNavigationBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;

	public TabNavigationBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		for (int direction : new int[]{1, -1}) {
			var action = direction == 1 ? ControlifyActions.GUI_NEXT_TAB : ControlifyActions.GUI_PREV_TAB;
			while (context.isCurrentScreen() && action.on(controller).pulse().consumePulse()) {
				Optional<TabNavigationBar> navBar = context.screen().children()
						.stream()
						.filter(TabNavigationBar.class::isInstance)
						.map(TabNavigationBar.class::cast)
						.findFirst();
				if (navBar.isEmpty()) {
					break;
				}
				var accessor = (TabNavigationBarAccessor) navBar.get();
				var tabs = accessor.controlify$getTabs();
				if (!tabs.isEmpty()) {
					int current = tabs.indexOf(accessor.controlify$getTabManager().getCurrentTab());
					navBar.get().selectTab(Math.floorMod(current + direction, tabs.size()), true);
					context.synchronizeFocus();
				}
			}
		}
		next.run();
	}
}
