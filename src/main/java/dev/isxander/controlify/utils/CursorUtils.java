package dev.isxander.controlify.utils;

import com.mojang.blaze3d.platform.Window;

//? if >=26.3 {
import org.lwjgl.sdl.SDLMouse;
//?} else {
/*import org.lwjgl.glfw.GLFW;
*///?}

public final class CursorUtils {

	private CursorUtils() {
	}

	public static void setVisibility(Window window, boolean visible) {
		//? if >=26.3 {
		if (visible) {
			SDLMouse.SDL_ShowCursor();
			SDLMouse.SDL_SetWindowRelativeMouseMode(window.handle(), false);
		} else {
			SDLMouse.SDL_HideCursor();
		}
		//?} else {
		/*GLFW.glfwSetInputMode(
			window.handle(),
			GLFW.GLFW_CURSOR,
			visible
				? GLFW.GLFW_CURSOR_NORMAL
				: GLFW.GLFW_CURSOR_HIDDEN
		);
		*///?}
	}

	public static void setPosition(Window window, float x, float y) {
		//? if >=26.3 {
		SDLMouse.SDL_WarpMouseInWindow(window.handle(), x, y);
		//?} else {
		/*GLFW.glfwSetCursorPos(window.handle(), x, y);
		*///?}
	}
}
