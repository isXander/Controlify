package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.yacl.api.NameableEnum;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public enum ControllerTheme implements NameableEnum {
    AUTO(c -> c.type().theme().id(c)),
    XBOX_ONE(c -> "xbox"),
    DUALSHOCK4(c -> "dualshock4");

    private final Function<Controller, String> idGetter;

    ControllerTheme(Function<Controller, String> idGetter) {
        this.idGetter = idGetter;
    }

    public String id(Controller controller) {
        return idGetter.apply(controller);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("controlify.controller_theme." + name().toLowerCase());
    }
}
