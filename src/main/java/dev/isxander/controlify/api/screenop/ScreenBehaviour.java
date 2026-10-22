package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.controller.Controller;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Consumer;

/// Stateful sidecar created once per screen instance. Continuations are synchronous
/// and may be called at most once. Navigation completes before interaction is routed.
public interface ScreenBehaviour {
	default void handleNavigation(Controller controller, Runnable next) {
		next.run();
	}

	default void handleInteraction(Controller controller, Runnable next) {
		next.run();
	}

	default void onWidgetRebuild(Runnable next) {
		next.run();
	}

	default void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller controller, Runnable next) {
		next.run();
	}

	default void collectSnapPoints(Consumer<SnapPoint> output, Runnable next) {
		next.run();
	}

	/// Per-frame update before GUI mouse processing. Delta is in realtime ticks.
	default void update(float delta, Runnable next) {
		next.run();
	}

	/// Lifetime notification, broadcast independently of input propagation. A screen
	/// context can become active repeatedly as overlays open and close.
	default void onScreenActiveChanged(boolean active) {
	}
}
