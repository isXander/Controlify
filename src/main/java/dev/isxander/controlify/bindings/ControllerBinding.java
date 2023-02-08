package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import net.minecraft.client.KeyMapping;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class ControllerBinding {
    private final Controller controller;
    private IBind bind;
    private final IBind defaultBind;
    private final ResourceLocation id;
    private final Component name, description;
    private final KeyMappingOverride override;

    private static final Map<Controller, Set<Bind>> pressedBinds = new HashMap<>();

    public ControllerBinding(Controller controller, IBind defaultBind, ResourceLocation id, KeyMapping override, BooleanSupplier toggleOverride) {
        this.controller = controller;
        this.bind = this.defaultBind = defaultBind;
        this.id = id;
        this.name = Component.translatable("controlify.binding." + id.getNamespace() + "." + id.getPath());
        var descKey = "controlify.binding." + id.getNamespace() + "." + id.getPath() + ".desc";
        this.description = Language.getInstance().has(descKey) ? Component.translatable(descKey) : Component.empty();
        this.override = override != null ? new KeyMappingOverride(override, toggleOverride) : null;
    }

    public ControllerBinding(Controller controller, IBind defaultBind, ResourceLocation id) {
        this(controller, defaultBind, id, null, () -> false);
    }

    public float state() {
        return bind.state(controller.state(), controller);
    }

    public boolean held() {
        return bind.held(controller.state(), controller);
    }

    public boolean justPressed() {
        if (hasBindPressed(this)) return false;

        if (held() && !bind.held(controller.prevState(), controller)) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    public boolean justReleased() {
        if (hasBindPressed(this)) return false;

        if (!held() && bind.held(controller.prevState(), controller)) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    public IBind currentBind() {
        return bind;
    }

    public void setCurrentBind(IBind bind) {
        this.bind = bind;
    }

    public IBind defaultBind() {
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

    public KeyMappingOverride override() {
        return override;
    }

    // FIXME: very hack solution please remove me

    public static void clearPressedBinds(Controller controller) {
        if (pressedBinds.containsKey(controller)) {
            pressedBinds.get(controller).clear();
        }
    }

    private static boolean hasBindPressed(ControllerBinding binding) {
        var pressed = pressedBinds.getOrDefault(binding.controller, Set.of());
        return pressed.containsAll(getBinds(binding.bind));
    }

    private static void addPressedBind(ControllerBinding binding) {
        pressedBinds.computeIfAbsent(binding.controller, c -> new HashSet<>()).addAll(getBinds(binding.bind));
    }

    private static Set<Bind> getBinds(IBind bind) {
        if (bind instanceof CompoundBind compoundBind) {
            return compoundBind.binds();
        } else {
            return Set.of((Bind) bind);
        }
    }

    public record KeyMappingOverride(KeyMapping keyMapping, BooleanSupplier toggleable) {
    }
}
