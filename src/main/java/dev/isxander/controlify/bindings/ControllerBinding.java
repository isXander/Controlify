package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;
import net.minecraft.network.chat.Component;

public class ControllerBinding {
    private final Controller controller;
    private final Bind bind;
    private final Component name, description;

    public ControllerBinding(Controller controller, Bind defaultBind, String id, Component description) {
        this.controller = controller;
        this.bind = defaultBind;
        this.name = Component.translatable("controlify.binding." + id);
        this.description = description;
    }

    public ControllerBinding(Controller controller, Bind defaultBind, String id) {
        this(controller, defaultBind, id, Component.empty());
    }

    public boolean held() {
        return bind.state(controller.state());
    }

    public boolean justPressed() {
        return held() && !bind.state(controller.prevState());
    }

    public boolean justReleased() {
        return !held() && bind.state(controller.prevState());
    }

    public Component name() {
        return name;
    }

    public Component description() {
        return description;
    }
}
