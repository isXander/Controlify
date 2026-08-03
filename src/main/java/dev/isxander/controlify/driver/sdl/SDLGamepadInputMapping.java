package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.sdl.SdlGamepad;
import dev.isxander.sdl.SdlGamepadHandle;
import net.minecraft.resources.Identifier;

public sealed interface SDLGamepadInputMapping {
	Identifier controlifyInput();

	boolean hasInput(SdlGamepad sdl, SdlGamepadHandle gamepad);

	void applyState(SdlGamepad sdl, SdlGamepadHandle gamepad, ControllerStateImpl state);

	record ButtonMapping(Identifier controlifyInput, int sdlButton) implements SDLGamepadInputMapping {
		@Override
		public boolean hasInput(SdlGamepad sdl, SdlGamepadHandle gamepad) {
			return sdl.SDL_GamepadHasButton(gamepad, sdlButton);
		}

		@Override
		public void applyState(SdlGamepad sdl, SdlGamepadHandle gamepad, ControllerStateImpl state) {
			if (hasInput(sdl, gamepad)) {
				state.setButton(controlifyInput, sdl.SDL_GetGamepadButton(gamepad, sdlButton));
			}
		}
	}

	record AxisMapping(Identifier controlifyInput, int sdlAxis, boolean positive) implements SDLGamepadInputMapping {
		@Override
		public boolean hasInput(SdlGamepad sdl, SdlGamepadHandle gamepad) {
			return sdl.SDL_GamepadHasAxis(gamepad, sdlAxis);
		}

		@Override
		public void applyState(SdlGamepad sdl, SdlGamepadHandle gamepad, ControllerStateImpl state) {
			if (hasInput(sdl, gamepad)) {
				float f = CUtil.mapShortToFloat(sdl.SDL_GetGamepadAxis(gamepad, sdlAxis));
				f = positive ? CUtil.positiveAxis(f) : CUtil.negativeAxis(f);

				state.setAxis(controlifyInput, f);
			}
		}
	}
}
