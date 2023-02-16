package dev.isxander.controlify.controller.gamepad;

import dev.isxander.yacl.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum BuiltinGamepadTheme implements NameableEnum {
    DEFAULT("default"),
    XBOX_ONE("xbox_one"),
    DUALSHOCK4("dualshock4");

    private final String id;

    BuiltinGamepadTheme(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("controlify.controller_theme." + id().toLowerCase());
    }
}
