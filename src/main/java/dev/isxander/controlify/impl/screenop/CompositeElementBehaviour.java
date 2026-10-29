/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ElementKeyboardBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.SnapPoint;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class CompositeElementBehaviour implements ElementBehaviour {
	private final List<ElementBehaviour> behaviours;
	private final boolean branch;

	public CompositeElementBehaviour(List<? extends ElementBehaviour> behaviours) {
		this(behaviours, false);
	}

	private CompositeElementBehaviour(List<? extends ElementBehaviour> behaviours, boolean branch) {
		this.behaviours = List.copyOf(behaviours);
		this.branch = branch;
	}

	public static ElementBehaviour branch(ElementBehaviour behaviour) {
		return new CompositeElementBehaviour(List.of(behaviour), true);
	}

	private void run(BiConsumer<ElementBehaviour, Runnable> invoke, Runnable next) {
		if (branch) {
			BehaviourChain.run(behaviours, invoke, () -> {
			});
			next.run();
		} else {
			BehaviourChain.run(behaviours, invoke, next);
		}
	}

	@Override
	public void handleNavigation(@NonNull ScreenContext<?> screen, @NonNull Controller controller, @NonNull Runnable next) {
		run((behaviour, continuation) -> {
			if (MinecraftUtil.getScreen() == screen.screen()) {
				behaviour.handleNavigation(screen, controller, continuation);
			}
		}, () -> {
			if (MinecraftUtil.getScreen() == screen.screen()) {
				next.run();
			}
		});
	}

	@Override
	public void handleInteraction(@NonNull ScreenContext<?> screen, @NonNull Controller controller, @NonNull Runnable next) {
		run((behaviour, continuation) -> {
			if (MinecraftUtil.getScreen() == screen.screen()) {
				behaviour.handleInteraction(screen, controller, continuation);
			}
		}, () -> {
			if (MinecraftUtil.getScreen() == screen.screen()) {
				next.run();
			}
		});
	}

	@Override
	public void onFocusChanged(@NonNull ScreenContext<?> screen, boolean focused) {
		behaviours.forEach(behaviour -> behaviour.onFocusChanged(screen, focused));
	}

	@Override
	public Optional<ElementKeyboardBehaviour> keyboardBehaviour(@NonNull ScreenContext<?> screen) {
		return behaviours.stream().map(behaviour -> behaviour.keyboardBehaviour(screen))
				.flatMap(Optional::stream).findFirst();
	}

	@Override
	public void collectSnapPoints(@NonNull ScreenContext<?> screen, @NonNull Consumer<SnapPoint> output, @NonNull Runnable next) {
		run((behaviour, continuation) -> behaviour.collectSnapPoints(screen, output, continuation), next);
	}
}
