package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class ControllerBinding {
    private final Controller controller;
    private Bind bind;
    private final Bind defaultBind;
    private final String id;
    private final Component name, description;
    private final KeyMapping override;

    public ControllerBinding(Controller controller, Bind defaultBind, String id, Component description, KeyMapping override) {
        this.controller = controller;
        this.bind = this.defaultBind = defaultBind;
        this.id = id;
        this.name = Component.translatable("controlify.binding." + id);
        this.description = description;
        this.override = override;
    }

    public ControllerBinding(Controller controller, Bind defaultBind, String id, KeyMapping override) {
        this(controller, defaultBind, id, Component.empty(), override);
    }

    public boolean held() {
        return bind.state(controller.state(), controller);
    }

    public boolean justPressed() {
        return held() && !bind.state(controller.prevState(), controller);
    }

    public boolean justReleased() {
        return !held() && bind.state(controller.prevState(), controller);
    }

    public Bind currentBind() {
        return bind;
    }

    public void setCurrentBind(Bind bind) {
        this.bind = bind;
    }

    public Bind defaultBind() {
        return defaultBind;
    }

    public String id() {
        return id;
    }

    public Component name() {
        return name;
    }

    public Component description() {
        return description;
    }

    public KeyMapping override() {
        return override;
    }
}
