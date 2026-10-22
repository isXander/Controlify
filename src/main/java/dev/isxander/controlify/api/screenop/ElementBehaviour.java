package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.controller.Controller;

import java.util.Optional;
import java.util.function.Consumer;

/// Stateful, per-element sidecar within one screen. Continuations are synchronous
/// and may be called at most once. Omitting one stops the remaining stage.
public interface ElementBehaviour {
	default void handleNavigation(ScreenContext<?> screen, Controller controller, Runnable next) {
		next.run();
	}

	default void handleInteraction(ScreenContext<?> screen, Controller controller, Runnable next) {
		next.run();
	}

	/// A lifecycle notification, including focus changes made by other elements.
	default void onFocusChanged(ScreenContext<?> screen, boolean focused) {
	}

	/// Empty inherits the next policy. DoNothing explicitly suppresses the popup
	/// and ordinary activation, for elements whose screen already supplies a keyboard.
	default Optional<ElementKeyboardBehaviour> keyboardBehaviour(ScreenContext<?> screen) {
		return Optional.empty();
	}

	/// Points use scaled screen coordinates. The continuation supplies the underlying
	/// behaviour: widget centres or child traversal for containers. Omitting it can
	/// replace an entire container's snap points, including its descendants.
	default void collectSnapPoints(ScreenContext<?> screen, Consumer<SnapPoint> output, Runnable next) {
		next.run();
	}
}
