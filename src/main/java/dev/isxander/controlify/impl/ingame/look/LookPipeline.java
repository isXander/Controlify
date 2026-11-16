/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.ingame.look.LookContext;
import dev.isxander.controlify.api.ingame.look.LookScaler;
import dev.isxander.controlify.api.ingame.look.LookSource;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

/// Turns one controller's look sources and scalers into a rotation each frame.
/// Sources add, scalers multiply, and the sink applies; see [LookAccumulator].
public final class LookPipeline {
	/// The frame time handed to the pipeline is in game ticks.
	private static final float TICKS_PER_SECOND = 20f;

	private final Controller controller;
	private final List<LookSource> sources;
	private final List<LookScaler> scalers;
	private final LookSink sink;
	private final LookAccumulator accumulator = new LookAccumulator();

	public LookPipeline(Controller controller, LookRegistryImpl registry, LookSink sink) {
		this.controller = controller;
		this.sources = registry.createSources(controller);
		this.scalers = registry.createScalers(controller);
		this.sink = sink;
	}

	/// @param player the player to turn
	/// @param deltaTicks the frame time in game ticks
	public void process(LocalPlayer player, float deltaTicks) {
		LookContext ctx = new LookContext(
				this.controller,
				player,
				deltaTicks / TICKS_PER_SECOND,
				player.getYRot(),
				player.getXRot()
		);

		this.accumulator.clear();
		for (LookSource source : this.sources) {
			source.contribute(ctx, this.accumulator.output());
		}
		for (LookScaler scaler : this.scalers) {
			scaler.scale(ctx, this.accumulator.scales());
		}

		LookAccumulator.Rotation rotation = this.accumulator.resolve(ctx.dt());
		this.sink.apply(rotation.yaw(), rotation.pitch());
	}

	/// Look processing was interrupted.
	public void reset() {
		for (LookSource source : this.sources) {
			source.reset();
		}
	}
}
