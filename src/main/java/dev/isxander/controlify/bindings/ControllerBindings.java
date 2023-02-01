package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.event.ControlifyEvents;
import dev.isxander.controlify.mixins.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControllerBindings {
    public final ControllerBinding JUMP, SNEAK, ATTACK, USE, SPRINT, NEXT_SLOT, PREV_SLOT, PAUSE, INVENTORY, CHANGE_PERSPECTIVE, OPEN_CHAT;

    private final List<ControllerBinding> registry = new ArrayList<>();

    public ControllerBindings(Controller controller) {
        var options = Minecraft.getInstance().options;

        JUMP = register(new ControllerBinding(controller, Bind.A_BUTTON, "jump", options.keyJump));
        SNEAK = register(new ControllerBinding(controller, Bind.RIGHT_STICK, "sneak", options.keyShift));
        ATTACK = register(new ControllerBinding(controller, Bind.RIGHT_TRIGGER, "attack", options.keyAttack));
        USE = register(new ControllerBinding(controller, Bind.LEFT_TRIGGER, "use", options.keyUse));
        SPRINT = register(new ControllerBinding(controller, Bind.LEFT_STICK, "sprint", options.keySprint));
        NEXT_SLOT = register(new ControllerBinding(controller, Bind.RIGHT_BUMPER, "next_slot", null));
        PREV_SLOT = register(new ControllerBinding(controller, Bind.LEFT_BUMPER, "prev_slot", null));
        PAUSE = register(new ControllerBinding(controller, Bind.START, "pause", null));
        INVENTORY = register(new ControllerBinding(controller, Bind.Y_BUTTON, "inventory", options.keyInventory));
        CHANGE_PERSPECTIVE = register(new ControllerBinding(controller, Bind.BACK, "change_perspective", options.keyTogglePerspective));
        OPEN_CHAT = register(new ControllerBinding(controller, Bind.DPAD_UP, "open_chat", options.keyChat));

        ControlifyEvents.CONTROLLER_BIND_REGISTRY.invoker().onRegisterControllerBinds(this);

        ControlifyEvents.CONTROLLER_STATE_UPDATED.register(this::imitateVanillaClick);
    }

    public ControllerBinding register(ControllerBinding binding) {
        registry.add(binding);
        return binding;
    }

    public List<ControllerBinding> registry() {
        return Collections.unmodifiableList(registry);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (var binding : registry()) {
            json.addProperty(binding.id(), binding.currentBind().identifier());
        }
        return json;
    }

    public void fromJson(JsonObject json) {
        for (var binding : registry()) {
            var bind = json.get(binding.id());
            if (bind == null) continue;
            binding.setCurrentBind(Bind.fromIdentifier(bind.getAsString()));
        }
    }

    private void imitateVanillaClick(Controller controller) {
        for (var binding : registry()) {
            KeyMapping vanillaKey = binding.override();
            if (vanillaKey == null) continue;

            var vanillaKeyCode = ((KeyMappingAccessor) vanillaKey).getKey();

            KeyMapping.set(vanillaKeyCode, binding.held());
            if (binding.justPressed()) KeyMapping.click(vanillaKeyCode);
        }
    }
}
