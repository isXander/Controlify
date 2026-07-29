/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils.render;

import dev.isxander.controlify.platform.client.PlatformClientUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//? if >=26.3 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?} else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
*///?}

public interface CGuiElementRenderState extends GuiElementRenderState {

	BaseRenderState baseState();

	@Override
	default @NotNull RenderPipeline pipeline() {
		return baseState().pipeline();
	}

	@Override
	default @NotNull TextureSetup textureSetup() {
		return baseState().textureSetup();
	}

	@Override
	default @Nullable ScreenRectangle scissorArea() {
		return baseState().scissorArea();
	}

	@Override
	default @Nullable ScreenRectangle bounds() {
		return baseState().bounds();
	}

	default void submit(GuiGraphicsExtractor graphics) {
		PlatformClientUtil.submitGuiElement(graphics, this);
	}
}
