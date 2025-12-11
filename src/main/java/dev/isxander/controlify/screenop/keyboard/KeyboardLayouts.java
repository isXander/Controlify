package dev.isxander.controlify.screenop.keyboard;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

/**
 * A collection of predefined keyboard layouts.
 * These layouts can be used in the on-screen keyboard.
 * They are defined by resource locations and can be retrieved
 * from the {@link Controlify#keyboardLayoutManager()}.
 */
public final class KeyboardLayouts {

    public static final Identifier FULL = CUtil.rl("full");
    public static final Identifier SIMPLE = CUtil.rl("simple");
    public static final Identifier SERVER_IP = CUtil.rl("server_ip");

    public static KeyboardLayoutWithId full() {
        return Controlify.instance().keyboardLayoutManager().getLayout(FULL);
    }

    public static KeyboardLayoutWithId simple() {
        return Controlify.instance().keyboardLayoutManager().getLayout(SIMPLE);
    }

    public static KeyboardLayoutWithId serverIp() {
        return Controlify.instance().keyboardLayoutManager().getLayout(SERVER_IP);
    }

    public static KeyboardLayoutWithId fallback() {
        return new KeyboardLayoutWithId(FallbackKeyboardLayout.QWERTY, FallbackKeyboardLayout.ID);
    }

    private KeyboardLayouts() {}
}
