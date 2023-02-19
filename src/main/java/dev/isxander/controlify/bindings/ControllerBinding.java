package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import net.minecraft.client.KeyMapping;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class ControllerBinding<T extends ControllerState> {
    private final Controller<T, ?> controller;
    private IBind<T> bind;
    private final IBind<T> defaultBind;
    private final ResourceLocation id;
    private final Component name, description;
    private final KeyMappingOverride override;

    private static final Map<Controller<?, ?>, Set<IBind<?>>> pressedBinds = new HashMap<>();

    public ControllerBinding(Controller<T, ?> controller, IBind<T> defaultBind, ResourceLocation id, KeyMapping override, BooleanSupplier toggleOverride) {
        this.controller = controller;
        this.bind = this.defaultBind = defaultBind;
        this.id = id;
        this.name = Component.translatable("controlify.binding." + id.getNamespace() + "." + id.getPath());
        var descKey = "controlify.binding." + id.getNamespace() + "." + id.getPath() + ".desc";
        this.description = Language.getInstance().has(descKey) ? Component.translatable(descKey) : Component.empty();
        this.override = override != null ? new KeyMappingOverride(override, toggleOverride) : null;
    }

    public ControllerBinding(Controller<T, ?> controller, IBind<T> defaultBind, ResourceLocation id) {
        this(controller, defaultBind, id, null, () -> false);
    }

    @SuppressWarnings("unchecked")
    public ControllerBinding(Controller<T, ?> controller, GamepadBinds defaultBind, ResourceLocation id, KeyMapping override, BooleanSupplier toggleOverride) {
        this(controller, controller instanceof GamepadController gamepad ? (IBind<T>) defaultBind.forGamepad(gamepad) : new EmptyBind<>(), id, override, toggleOverride);
    }

    public ControllerBinding(Controller<T, ?> controller, GamepadBinds defaultBind, ResourceLocation id) {
        this(controller, defaultBind, id, null, () -> false);
    }

    public float state() {
        return bind.state(controller.state());
    }

    public float prevState() {
        return bind.state(controller.prevState());
    }

    public boolean held() {
        return bind.held(controller.state());
    }

    public boolean prevHeld() {
        return bind.held(controller.prevState());
    }

    public boolean justPressed() {
        if (hasBindPressed(this)) return false;

        if (held() && !prevHeld()) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    public boolean justReleased() {
        if (hasBindPressed(this)) return false;

        if (!held() && prevHeld()) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    public IBind<T> currentBind() {
        return bind;
    }

    public void setCurrentBind(IBind<T> bind) {
        this.bind = bind;
    }

    public IBind<T> defaultBind() {
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

    public boolean unbound() {
        return bind instanceof EmptyBind;
    }

    public KeyMappingOverride override() {
        return override;
    }

    // FIXME: very hack solution please remove me

    public static void clearPressedBinds(Controller<?, ?> controller) {
        if (pressedBinds.containsKey(controller)) {
            pressedBinds.get(controller).clear();
        }
    }

    private static boolean hasBindPressed(ControllerBinding<?> binding) {
        var pressed = pressedBinds.getOrDefault(binding.controller, Set.of());
        return pressed.containsAll(getBinds(binding.bind));
    }

    private static void addPressedBind(ControllerBinding<?> binding) {
        pressedBinds.computeIfAbsent(binding.controller, c -> new HashSet<>()).addAll(getBinds(binding.bind));
    }

    private static Set<IBind<?>> getBinds(IBind<?> bind) {
        return Set.of(bind);
    }

    public record KeyMappingOverride(KeyMapping keyMapping, BooleanSupplier toggleable) {
    }
}
