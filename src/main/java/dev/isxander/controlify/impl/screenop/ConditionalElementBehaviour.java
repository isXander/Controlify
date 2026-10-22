package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ElementKeyboardBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.SnapPoint;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConditionalElementBehaviour implements ElementBehaviour {
	private final BooleanSupplier enabled;
	private final ElementBehaviour behaviour;

	public ConditionalElementBehaviour(BooleanSupplier enabled, ElementBehaviour behaviour) {
		this.enabled = enabled;
		this.behaviour = behaviour;
	}

	@Override
	public void handleNavigation(ScreenContext<?> screen, Controller controller, Runnable next) {
		if (this.enabled.getAsBoolean()) {
			this.behaviour.handleNavigation(screen, controller, next);
		} else {
			next.run();
		}
	}

	@Override
	public void handleInteraction(ScreenContext<?> screen, Controller controller, Runnable next) {
		if (this.enabled.getAsBoolean()) {
			this.behaviour.handleInteraction(screen, controller, next);
		} else {
			next.run();
		}
	}

	@Override
	public void onFocusChanged(ScreenContext<?> screen, boolean focused) {
		this.behaviour.onFocusChanged(screen, focused);
	}

	@Override
	public Optional<ElementKeyboardBehaviour> keyboardBehaviour(ScreenContext<?> screen) {
		if (this.enabled.getAsBoolean()) {
			return this.behaviour.keyboardBehaviour(screen);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void collectSnapPoints(ScreenContext<?> screen, Consumer<SnapPoint> output, Runnable next) {
		if (this.enabled.getAsBoolean()) {
			this.behaviour.collectSnapPoints(screen, output, next);
		} else {
			next.run();
		}
	}
}
