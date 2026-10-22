package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.impl.action.ControlifyActions;
import net.minecraft.client.gui.components.Button;

import java.util.function.Supplier;

public final class DoneButtonGuideBehaviour implements ScreenBehaviour {
	private final ScreenContext<?> context;
	private final Supplier<@org.jetbrains.annotations.Nullable Button> button;

	public DoneButtonGuideBehaviour(ScreenContext<?> context, Supplier<@org.jetbrains.annotations.Nullable Button> button) {
		this.context = context;
		this.button = button;
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		var done = button.get();
		if (done != null) {
			context.buttonGuide().addGuideToButton(done, ControlifyActions.GUI_BACK, ButtonGuidePredicate.always());
		}
		next.run();
	}
}
