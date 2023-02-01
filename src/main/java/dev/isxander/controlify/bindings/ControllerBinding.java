package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ControllerBinding {
    private final Controller controller;
    private Bind bind;
    private final Bind defaultBind;
    private final ResourceLocation id;
    private final Component name, description;
    private final KeyMapping override;

    public ControllerBinding(Controller controller, Bind defaultBind, ResourceLocation id, Component description, KeyMapping override) {
        this.controller = controller;
        this.bind = this.defaultBind = defaultBind;
        this.id = id;
        this.name = Component.translatable("controlify.binding." + id.getNamespace() + "." + id.getPath());
        this.description = description;
        this.override = override;
    }

    public ControllerBinding(Controller controller, Bind defaultBind, ResourceLocation id, KeyMapping override) {
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

    public ResourceLocation id() {
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
