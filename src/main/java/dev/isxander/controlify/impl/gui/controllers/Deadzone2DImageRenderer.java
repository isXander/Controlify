/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.controllers;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.input.mapping.DeadzoneGroup;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.impl.utils.render.elements.CircleElementRenderState;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.Supplier;

public class Deadzone2DImageRenderer implements ImageRenderer {
	private final DeadzoneGroup deadzoneGroup;
	private final Supplier<Option<Float>> deadzoneOption;

	private final float[] directions;
	private final InputComponentImpl input;
	private EventSource.Subscription pipelineSubscription;

	public Deadzone2DImageRenderer(InputComponentImpl input, DeadzoneGroup deadzoneGroup, Supplier<Option<Float>> deadzoneOption) {
		this.deadzoneGroup = deadzoneGroup;
		this.deadzoneOption = deadzoneOption;
		this.directions = new float[4];
		Arrays.fill(this.directions, 0f);
		this.input = input;
	}

	/// Subscribed on first render rather than on construction, because a renderer is built for every
	/// config screen build but only rendered while visible. The listener holds the renderer weakly and
	/// unsubscribes itself once the renderer is gone, even if close() is never called.
	private void ensureSubscribed() {
		if (this.pipelineSubscription != null) {
			return;
		}
		WeakReference<Deadzone2DImageRenderer> self = new WeakReference<>(this);
		EventSource.Subscription[] subscription = new EventSource.Subscription[1];
		// Mapped, pre-deadzone values: ids match the deadzone group's axes, and the preview shows the raw stick position.
		subscription[0] = this.input.inputPipeline().sourceMappedPhysicalSignals().subscribe(signal -> {
			Deadzone2DImageRenderer renderer = self.get();
			if (renderer == null) {
				if (subscription[0] != null) {
					subscription[0].close();
				}
				return;
			}
			renderer.onSignal(signal);
		});
		this.pipelineSubscription = subscription[0];
	}

	@Override
	public int render(GuiGraphicsExtractor graphics, int x, int y, int renderWidth, float tickDelta) {
		this.ensureSubscribed();
		float radius = renderWidth / 4f;
		int renderHeight = (int) (radius * 2f);

		// axes go up -> down -> left -> right
		float up = this.directions[0];
		float down = this.directions[1];
		float left = this.directions[2];
		float right = this.directions[3];
		float currentX = right - left;
		float currentY = down - up;

		// axis lines
		graphics.horizontalLine(x, (int)(x + radius*2), (int) (y + radius), 0xFFAAAAAA);
		graphics.verticalLine((int) (x + radius), y, (int)(y + radius*2), 0xFFAAAAAA);

		// 100% outline
		CircleElementRenderState.outline(
				graphics,
				x + radius, y + radius,
				radius, 1f,
				-1
		).submit(graphics);
		// deadzone outline
		float deadzone = deadzoneOption.get().pendingValue();
		boolean aboveDeadzone = Math.abs(currentX) > deadzone || Math.abs(currentY) > deadzone;
		CircleElementRenderState.outline(
				graphics,
				x + radius, y + radius,
				deadzone * radius, 1f,
				aboveDeadzone ? 0xFF00FFFF : 0xFFFF0000
		).submit(graphics);

		// current axis point
		CircleElementRenderState.filled(
				graphics,
				x + radius + currentX * radius,
				y + radius + currentY * radius,
				1f, 0xFF00FF00
		).submit(graphics);

		Font font = Minecraft.getInstance().font;
		DecimalFormat format = new DecimalFormat("0.000");
		graphics.text(font, "X: " + format.format(currentX), (int) (x + radius * 2 + 5), y, -1);
		graphics.text(font, "Y: " + format.format(currentY), (int) (x + radius * 2 + 5), y + font.lineHeight + 1, -1);

		return renderHeight;
	}

	private void onSignal(PhysicalSignal signal) {
		if (signal instanceof PhysicalInputSignal.AxisMoved axisMoved) {
			int index = this.deadzoneGroup.axes().indexOf(axisMoved.input());
			if (index != -1) {
				this.directions[index] = axisMoved.value();
			}
		}
	}

	@Override
	public void close() {
		if (this.pipelineSubscription != null) {
			this.pipelineSubscription.close();
			this.pipelineSubscription = null;
		}
	}
}
