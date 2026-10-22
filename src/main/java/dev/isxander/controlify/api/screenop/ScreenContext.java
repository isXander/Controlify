package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.ButtonGuideApi;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ScreenContext<T extends Screen> {
	T screen();

	/// Resolves and caches the effective behaviour chain by element identity within
	/// this screen. Available after element registration completes.
	ElementBehaviour elementBehaviour(GuiEventListener element);

	InputMode inputMode();

	VirtualMouse virtualMouse();

	default boolean isVirtualMouseEnabled() {
		return virtualMouse().isEnabled();
	}

	/// Immutable snapshot, deepest focused element first.
	List<GuiEventListener> focusPath();

	void clearFocus();

	void setInitialFocus();

	/// Reports a navigation step to the input system, plays navigation haptics and,
	/// when extra UI sounds are enabled, the focus-change sound.
	void notifyNavigation(Controller controller);

	void playNavigationHaptic(Controller controller);

	/// Plays the focus-change sound directly, regardless of the extra UI sounds setting.
	void playFocusChangeSound();

	void playClickSound();

	ButtonGuideApi buttonGuide();

	/// Opens the on-screen keyboard for a supported element. True means handled (including
	/// intentional suppression); false allows ordinary activation to continue.
	boolean tryOpenKeyboard(
			Controller controller,
			@Nullable GuiEventListener component
	);
}
