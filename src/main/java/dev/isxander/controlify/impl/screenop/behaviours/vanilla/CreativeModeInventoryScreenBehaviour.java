/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.platform.client.CreativeTabHelper;
import dev.isxander.controlify.impl.platform.client.PlatformClientUtil;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

import java.util.List;

public final class CreativeModeInventoryScreenBehaviour implements ScreenBehaviour {
	private final CreativeTabHelper tabHelper;

	private final ScreenContext<? extends CreativeModeInventoryScreen> context;

	public CreativeModeInventoryScreenBehaviour(ScreenContext<? extends CreativeModeInventoryScreen> context) {
		this.context = context;
		this.tabHelper = PlatformClientUtil.createCreativeTabHelper(context.screen());
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		next.run();
		context.virtualMouse().snapToClosestPoint();
	}

	@SuppressWarnings("UnreachableCode")
	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		if (!context.isVirtualMouseEnabled()) {
			next.run();
			return;
		}
		List<CreativeModeTab> tabs = tabHelper.getTabsForPage(tabHelper.getCurrentPage());
		if (tabs.isEmpty()) {
			next.run();
			return;
		}
		while (ControlifyActions.GUI_NEXT_TAB.on(controller).pulse().consumePulse()) {
			int newIndex = tabs.indexOf(tabHelper.getSelectedTab()) + 1;
			if (newIndex >= tabs.size()) {
				newIndex = 0;

				int newPage = tabHelper.getCurrentPage() + 1;
				if (newPage >= tabHelper.getPageCount()) {
					newPage = 0;
				}

				tabHelper.setCurrentPage(newPage);
				tabs = tabHelper.getTabsForPage(newPage);
			}

			tabHelper.setSelectedTab(tabs.get(newIndex));
		}
		while (ControlifyActions.GUI_PREV_TAB.on(controller).pulse().consumePulse()) {
			int newIndex = tabs.indexOf(tabHelper.getSelectedTab()) - 1;
			if (newIndex < 0) {
				int newPage = tabHelper.getCurrentPage() - 1;
				if (newPage < 0) {
					newPage = tabHelper.getPageCount() - 1;
				}

				tabHelper.setCurrentPage(newPage);
				tabs = tabHelper.getTabsForPage(newPage);
				newIndex = tabs.size() - 1;
			}

			tabHelper.setSelectedTab(tabs.get(newIndex));
		}

		next.run();
	}

	@Override
	public void collectSnapPoints(java.util.function.Consumer<SnapPoint> output, Runnable next) {
		((Access) context.screen()).controlify$collectTabSnapPoints(tabHelper.getTabsForPage(tabHelper.getCurrentPage()), output);
		next.run();
	}

	public interface Access {
		void controlify$collectTabSnapPoints(List<CreativeModeTab> tabs, java.util.function.Consumer<SnapPoint> output);
	}

}
