package dev.isxander.controlify.bindings;

import dev.isxander.controlify.Controlify;

import java.util.Set;

public final class BindContexts {
    public static final BindContext
            INGAME = ctx("ingame"),
            GUI = ctx("gui"),
            GUI_VMOUSE_CURSOR_ONLY = ctx("gui_vmouse_cursor"),
            GUI_VMOUSE = ctx("gui_vmouse", GUI_VMOUSE_CURSOR_ONLY),
            CONTROLIFY_CONFIG = ctx("controlify_config", GUI),
            INVENTORY = ctx("inventory");

    private static BindContext ctx(String path, BindContext... parents) {
        return new BindContext(Controlify.id(path), Set.of(parents));
    }
}
