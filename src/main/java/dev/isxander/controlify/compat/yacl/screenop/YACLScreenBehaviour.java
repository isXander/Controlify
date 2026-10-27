/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat.yacl.screenop;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.action.BuiltinActions;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.compat.yacl.mixins.YACLScreenCategoryTabAccessor;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.WidgetAndType;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.input.KeyEvent;

public final class YACLScreenBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends YACLScreen> context;
	private final YACLScreen screen;

	public YACLScreenBehaviour(ScreenContext<? extends YACLScreen> context) {
		this.context = context;
		this.screen = context.screen();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (BuiltinActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			if (screen.tabManager.getCurrentTab() instanceof YACLScreen.CategoryTab categoryTab) {
				((YACLScreenCategoryTabAccessor) categoryTab).getSaveFinishedButton().onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
			}
			context.playClickSound();
		}

		next.run();
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		var previous = screen.tabManager.getCurrentTab();
		next.run();
		if (dev.isxander.controlify.impl.utils.MinecraftUtil.getScreen() != screen
				|| screen.tabManager.getCurrentTab() == previous) {
			return;
		}
		if (screen.tabManager.getCurrentTab() instanceof YACLScreen.CategoryTab categoryTab) {
			WidgetAndType<OptionListWidget> optionListHolder = ((YACLScreenCategoryTabAccessor) categoryTab).getOptionList();
			OptionListWidget optionList = optionListHolder.getType();
			optionList.setScrollAmount(0);

			screen.setFocused(optionListHolder.getWidget());

			if (optionList.children().isEmpty()) {
				return;
			}

			for (OptionListWidget.Entry entry : optionList.children()) {
				entry.setFocused(false);
				entry.setFocused(null);
			}

			OptionListWidget.Entry firstEntry = optionList.children().get(0);
			optionList.setFocused(firstEntry);

			if (!firstEntry.children().isEmpty()) {
				firstEntry.setFocused(firstEntry.children().get(0));
			}
		}
	}
}
