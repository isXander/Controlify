/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.SnapPoint;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class CompositeScreenBehaviour implements ScreenBehaviour {
	private final List<ScreenBehaviour> behaviours;
	private final boolean branch;

	public CompositeScreenBehaviour(List<? extends ScreenBehaviour> behaviours) {
		this(behaviours, false);
	}

	private CompositeScreenBehaviour(List<? extends ScreenBehaviour> behaviours, boolean branch) {
		this.behaviours = List.copyOf(behaviours);
		this.branch = branch;
	}

	public static ScreenBehaviour branch(ScreenBehaviour behaviour) {
		return new CompositeScreenBehaviour(List.of(behaviour), true);
	}

	private void run(BiConsumer<ScreenBehaviour, Runnable> invoke, Runnable next) {
		if (branch) {
			BehaviourChain.run(behaviours, invoke, () -> {
			});
			next.run();
		} else {
			BehaviourChain.run(behaviours, invoke, next);
		}
	}

	private void input(BiConsumer<ScreenBehaviour, Runnable> invoke, Runnable next) {
		var screen = MinecraftUtil.getScreen();
		run((behaviour, continuation) -> {
			if (MinecraftUtil.getScreen() == screen) {
				invoke.accept(behaviour, continuation);
			}
		}, () -> {
			if (MinecraftUtil.getScreen() == screen) {
				next.run();
			}
		});
	}

	@Override
	public void handleNavigation(@NonNull Controller controller, @NonNull Runnable next) {
		input((behaviour, continuation) -> behaviour.handleNavigation(controller, continuation), next);
	}

	@Override
	public void handleInteraction(@NonNull Controller controller, @NonNull Runnable next) {
		input((behaviour, continuation) -> behaviour.handleInteraction(controller, continuation), next);
	}

	@Override
	public void onWidgetRebuild(@NonNull Runnable next) {
		run(ScreenBehaviour::onWidgetRebuild, next);
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, float tickDelta, @NonNull Controller controller, @NonNull Runnable next) {
		run((behaviour, continuation) -> behaviour.extractRenderState(graphics, tickDelta, controller, continuation), next);
	}

	@Override
	public void collectSnapPoints(@NonNull Consumer<SnapPoint> output, @NonNull Runnable next) {
		run((behaviour, continuation) -> behaviour.collectSnapPoints(output, continuation), next);
	}

	@Override
	public void update(float delta, Runnable next) {
		input((behaviour, continuation) -> behaviour.update(delta, continuation), next);
	}

	@Override
	public void onScreenActiveChanged(boolean active) {
		behaviours.forEach(behaviour -> behaviour.onScreenActiveChanged(active));
	}
}
