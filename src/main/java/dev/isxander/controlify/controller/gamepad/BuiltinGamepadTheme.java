package dev.isxander.controlify.controller.gamepad;

import dev.isxander.yacl.api.NameableEnum;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public enum BuiltinGamepadTheme implements NameableEnum {
    DEFAULT("default", "default"),
    XBOX_ONE("Xbox One", "xbox_one"),
    DUALSHOCK4("Dualshock 4", "dualshock4"),
    DUALSHOCK3("Dualshock 3", "dualshock3"),
    DUALSENSE("Dualsense", "dualsense"),
    STEAM_DECK("Steam Deck", "steam_deck");

    private final String name, id;

    BuiltinGamepadTheme(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public Component getDisplayName() {
        if (this == DEFAULT)
            return Component.translatable("options.gamma.default");

        return Component.literal(name);
    }
}
