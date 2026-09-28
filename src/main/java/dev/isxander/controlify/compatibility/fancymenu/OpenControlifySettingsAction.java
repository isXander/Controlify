/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compatibility.fancymenu;

//? if fancy_menu {

/*import de.keksuccino.fancymenu.customization.action.Action;
import dev.isxander.controlify.gui.screen.ControlifySettingsScreen;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenControlifySettingsAction extends Action {
	public OpenControlifySettingsAction() {
		super("controlify:open-settings");
	}

	@Override
	public boolean hasValue() {
		return false;
	}

	@Override
	public void execute(@Nullable String s) {
		MinecraftUtil.setScreen(new ControlifySettingsScreen(MinecraftUtil.getScreen()));
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("controlify.gui.button");
	}

	@Override
	public @NotNull Component getDescription() {
		return Component.empty();
	}

	@Override
	public @Nullable Component getValueDisplayName() {
		return null;
	}

	@Override
	public @Nullable String getValuePreset() {
		return "";
	}

}
*///?}
