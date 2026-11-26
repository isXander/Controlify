/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils.render;

//? if >=26.4 {
import net.minecraft.client.gui.GuiPipeline;
//?} else if >=26.3 {
/*import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
*///?} else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
*///?}

/// The GUI pipelines Controlify draws with, under one name on every Minecraft version.
/// 26.4 replaced the `RenderPipelines.GUI*` pipelines with `GuiPipeline` handles.
public final class GuiPipelines {
	//? if >=26.4 {
	public static final GuiPipeline GUI = GuiPipeline.DEFAULT;
	public static final GuiPipeline GUI_TEXTURED = GuiPipeline.TEXTURED;
	//?} else {
	/*public static final RenderPipeline GUI = RenderPipelines.GUI;
	public static final RenderPipeline GUI_TEXTURED = RenderPipelines.GUI_TEXTURED;
	*///?}

	private GuiPipelines() {
	}
}
