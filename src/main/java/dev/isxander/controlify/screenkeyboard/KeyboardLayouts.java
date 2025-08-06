package dev.isxander.controlify.screenkeyboard;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public final class KeyboardLayouts {

    public static final ResourceLocation CHAT = CUtil.rl("chat");

    public static KeyboardLayoutWithId chat() {
        return Controlify.instance().keyboardLayoutManager().getLayout(CHAT);
    }

    public static KeyboardLayoutWithId fallback() {
        return new KeyboardLayoutWithId(FallbackKeyboardLayout.QWERTY, FallbackKeyboardLayout.ID);
    }

    private KeyboardLayouts() {}
}
