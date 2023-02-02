package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.event.ControlifyEvents;
import dev.isxander.controlify.mixins.feature.bind.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ControllerBindings {
    public final ControllerBinding
            JUMP, SNEAK,
            ATTACK, USE,
            SPRINT,
            DROP,
            NEXT_SLOT, PREV_SLOT,
            PAUSE,
            INVENTORY,
            CHANGE_PERSPECTIVE,
            OPEN_CHAT,
            GUI_PRESS, GUI_BACK,
            VMOUSE_LCLICK, VMOUSE_RCLICK, VMOUSE_MCLICK, VMOUSE_ESCAPE, VMOUSE_TOGGLE;

    private final Map<ResourceLocation, ControllerBinding> registry = new LinkedHashMap<>();

    public ControllerBindings(Controller controller) {
        var options = Minecraft.getInstance().options;

        register(JUMP = new ControllerBinding(controller, Bind.A_BUTTON, new ResourceLocation("controlify", "jump"), options.keyJump));
        register(SNEAK = new ControllerBinding(controller, Bind.RIGHT_STICK, new ResourceLocation("controlify", "sneak"), options.keyShift));
        register(ATTACK = new ControllerBinding(controller, Bind.RIGHT_TRIGGER, new ResourceLocation("controlify", "attack"), options.keyAttack));
        register(USE = new ControllerBinding(controller, Bind.LEFT_TRIGGER, new ResourceLocation("controlify", "use"), options.keyUse));
        register(SPRINT = new ControllerBinding(controller, Bind.LEFT_STICK, new ResourceLocation("controlify", "sprint"), options.keySprint));
        register(DROP = new ControllerBinding(controller, Bind.DPAD_DOWN, new ResourceLocation("controlify", "drop"), options.keyDrop));
        register(NEXT_SLOT = new ControllerBinding(controller, Bind.RIGHT_BUMPER, new ResourceLocation("controlify", "next_slot"), null));
        register(PREV_SLOT = new ControllerBinding(controller, Bind.LEFT_BUMPER, new ResourceLocation("controlify", "prev_slot"), null));
        register(PAUSE = new ControllerBinding(controller, Bind.START, new ResourceLocation("controlify", "pause"), null));
        register(INVENTORY = new ControllerBinding(controller, Bind.Y_BUTTON, new ResourceLocation("controlify", "inventory"), options.keyInventory));
        register(CHANGE_PERSPECTIVE = new ControllerBinding(controller, Bind.BACK, new ResourceLocation("controlify", "change_perspective"), options.keyTogglePerspective));
        register(OPEN_CHAT = new ControllerBinding(controller, Bind.DPAD_UP, new ResourceLocation("controlify", "open_chat"), options.keyChat));
        register(GUI_PRESS = new ControllerBinding(controller, Bind.A_BUTTON, new ResourceLocation("controlify", "gui_press"), null));
        register(GUI_BACK = new ControllerBinding(controller, Bind.B_BUTTON, new ResourceLocation("controlify", "gui_back"), null));
        register(VMOUSE_LCLICK = new ControllerBinding(controller, Bind.A_BUTTON, new ResourceLocation("controlify", "vmouse_lclick"), null));
        register(VMOUSE_RCLICK = new ControllerBinding(controller, Bind.X_BUTTON, new ResourceLocation("controlify", "vmouse_rclick"), null));
        register(VMOUSE_MCLICK = new ControllerBinding(controller, Bind.Y_BUTTON, new ResourceLocation("controlify", "vmouse_mclick"), null));
        register(VMOUSE_ESCAPE = new ControllerBinding(controller, Bind.B_BUTTON, new ResourceLocation("controlify", "vmouse_escape"), null));
        register(VMOUSE_TOGGLE = new ControllerBinding(controller, Bind.BACK, new ResourceLocation("controlify", "vmouse_toggle"), null));

        ControlifyEvents.CONTROLLER_BIND_REGISTRY.invoker().onRegisterControllerBinds(this, controller);

        ControlifyEvents.CONTROLLER_STATE_UPDATED.register(this::imitateVanillaClick);
        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> KeyMapping.releaseAll());
    }

    public BindingSupplier register(ControllerBinding binding) {
        registry.put(binding.id(), binding);
        return controller -> controller.bindings().get(binding.id());
    }

    public ControllerBinding get(ResourceLocation id) {
        return registry.get(id);
    }

    public Map<ResourceLocation, ControllerBinding> registry() {
        return Collections.unmodifiableMap(registry);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (var binding : registry().values()) {
            json.addProperty(binding.id().toString(), binding.currentBind().identifier());
        }
        return json;
    }

    public void fromJson(JsonObject json) {
        for (var binding : registry().values()) {
            var bind = json.get(binding.id().toString());
            if (bind == null) continue;
            binding.setCurrentBind(Bind.fromIdentifier(bind.getAsString()));
        }
    }

    private void imitateVanillaClick(Controller controller) {
        if (Controlify.instance().currentInputMode() != InputMode.CONTROLLER)
            return;

        for (var binding : registry().values()) {
            KeyMapping vanillaKey = binding.override();
            if (vanillaKey == null) continue;

            var vanillaKeyCode = ((KeyMappingAccessor) vanillaKey).getKey();

            KeyMapping.set(vanillaKeyCode, binding.held());
            if (binding.justPressed()) KeyMapping.click(vanillaKeyCode);
        }
    }
}
