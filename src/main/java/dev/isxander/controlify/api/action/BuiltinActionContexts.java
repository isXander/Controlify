package dev.isxander.controlify.api.action;

import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

/// IDs of Controlify's built-in action contexts. Use these directly in {@link ActionSpecBuilder#context(Identifier...)}.
/// Referencing these constants does not initialize or register context implementations.
public final class BuiltinActionContexts {
	/// Always applicable.
	public static final Identifier UNKNOWN = CUtil.rl("unknown");
	public static final Identifier IN_GAME = CUtil.rl("in_game");
	public static final Identifier ANY_SCREEN = CUtil.rl("screen");
	public static final Identifier REGULAR_SCREEN = CUtil.rl("regular_screen");
	public static final Identifier CONTAINER = CUtil.rl("container");
	public static final Identifier V_MOUSE_CURSOR = CUtil.rl("vmouse_cursor");
	public static final Identifier V_MOUSE_COMPAT = CUtil.rl("vmouse_compat");
	public static final Identifier RADIAL_MENU = CUtil.rl("radial_menu");

	private BuiltinActionContexts() {
	}
}
