/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.bindings.ControlifyBindings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.platform.client.CreativeTabHelper;
import dev.isxander.controlify.impl.platform.client.PlatformClientUtil;
import dev.isxander.controlify.impl.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CreativeModeInventoryScreenProcessor extends AbstractContainerScreenProcessor<CreativeModeInventoryScreen> {
	private final CreativeTabHelper tabHelper;

	public CreativeModeInventoryScreenProcessor(CreativeModeInventoryScreen screen, Supplier<Slot> hoveredSlot, ClickSlotFunction clickSlotFunction, Predicate<ControllerImpl> itemSlotAction) {
		super(screen, hoveredSlot, clickSlotFunction, itemSlotAction);
		this.tabHelper = PlatformClientUtil.createCreativeTabHelper(screen);
	}

	@Override
	public void onWidgetRebuild() {
		super.onWidgetRebuild();
		Controlify.instance().virtualMouseHandler().snapToClosestPoint();
	}

	@SuppressWarnings("UnreachableCode")
	@Override
	protected void handleScreenVMouse(ControllerImpl controller, VirtualMouseHandler vmouse) {
		List<CreativeModeTab> tabs = tabHelper.getTabsForPage(tabHelper.getCurrentPage());
		if (ControlifyBindings.GUI_NEXT_TAB.on(controller).justPressed()) {
			int newIndex = tabs.indexOf(tabHelper.getSelectedTab()) + 1;
			if (newIndex >= tabs.size()) {
				newIndex = 0;

				int newPage = tabHelper.getCurrentPage() + 1;
				if (newPage >= tabHelper.getPageCount())
					newPage = 0;

				tabHelper.setCurrentPage(newPage);
				tabs = tabHelper.getTabsForPage(newPage);
			}

			tabHelper.setSelectedTab(tabs.get(newIndex));
		}
		if (ControlifyBindings.GUI_PREV_TAB.on(controller).justPressed()) {
			int newIndex = tabs.indexOf(tabHelper.getSelectedTab()) - 1;
			if (newIndex < 0) {
				int newPage = tabHelper.getCurrentPage() - 1;
				if (newPage < 0)
					newPage = tabHelper.getPageCount() - 1;

				tabHelper.setCurrentPage(newPage);
				tabs = tabHelper.getTabsForPage(newPage);
				newIndex = tabs.size() - 1;
			}

			tabHelper.setSelectedTab(tabs.get(newIndex));
		}

		super.handleScreenVMouse(controller, vmouse);
	}
}
