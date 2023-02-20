package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.event.ControlifyEvents;
import dev.isxander.controlify.mixins.feature.bind.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ControllerBindings<T extends ControllerState> {
    public final ControllerBinding<T>
            WALK_FORWARD, WALK_BACKWARD, WALK_LEFT, WALK_RIGHT,
            LOOK_UP, LOOK_DOWN, LOOK_LEFT, LOOK_RIGHT,
            JUMP, SNEAK,
            ATTACK, USE,
            SPRINT,
            DROP,
            NEXT_SLOT, PREV_SLOT,
            PAUSE,
            INVENTORY,
            CHANGE_PERSPECTIVE,
            SWAP_HANDS,
            OPEN_CHAT,
            GUI_PRESS, GUI_BACK,
            GUI_NEXT_TAB, GUI_PREV_TAB,
            PICK_BLOCK,
            TOGGLE_HUD_VISIBILITY,
            SHOW_PLAYER_LIST,
            VMOUSE_MOVE_UP, VMOUSE_MOVE_DOWN, VMOUSE_MOVE_LEFT, VMOUSE_MOVE_RIGHT,
            VMOUSE_LCLICK, VMOUSE_RCLICK, VMOUSE_SHIFT_CLICK,
            VMOUSE_SCROLL_UP, VMOUSE_SCROLL_DOWN,
            VMOUSE_ESCAPE, VMOUSE_SHIFT,
            VMOUSE_TOGGLE,
            GUI_NAVI_UP, GUI_NAVI_DOWN, GUI_NAVI_LEFT, GUI_NAVI_RIGHT,
            CYCLE_OPT_FORWARD, CYCLE_OPT_BACKWARD;

    private final Map<ResourceLocation, ControllerBinding<T>> registry = new LinkedHashMap<>();

    private final Controller<T, ?> controller;

    public ControllerBindings(Controller<T, ?> controller) {
        this.controller = controller;
        var options = Minecraft.getInstance().options;

        register(WALK_FORWARD = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_FORWARD, new ResourceLocation("controlify", "walk_forward")));
        register(WALK_BACKWARD = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_BACKWARD, new ResourceLocation("controlify", "walk_backward")));
        register(WALK_LEFT = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_LEFT, new ResourceLocation("controlify", "strafe_left")));
        register(WALK_RIGHT = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_RIGHT, new ResourceLocation("controlify", "strafe_right")));
        register(LOOK_UP = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_FORWARD, new ResourceLocation("controlify", "look_up")));
        register(LOOK_DOWN = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_BACKWARD, new ResourceLocation("controlify", "look_down")));
        register(LOOK_LEFT = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_LEFT, new ResourceLocation("controlify", "look_left")));
        register(LOOK_RIGHT = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_RIGHT, new ResourceLocation("controlify", "look_right")));
        register(JUMP = new ControllerBinding<>(controller, GamepadBinds.A_BUTTON, new ResourceLocation("controlify", "jump"), options.keyJump, () -> false));
        register(SNEAK = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_PRESS, new ResourceLocation("controlify", "sneak"), options.keyShift, () -> controller.config().toggleSneak));
        register(ATTACK = new ControllerBinding<>(controller, GamepadBinds.RIGHT_TRIGGER, new ResourceLocation("controlify", "attack"), options.keyAttack, () -> false));
        register(USE = new ControllerBinding<>(controller, GamepadBinds.LEFT_TRIGGER, new ResourceLocation("controlify", "use"), options.keyUse, () -> false));
        register(SPRINT = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_PRESS, new ResourceLocation("controlify", "sprint"), options.keySprint, () -> controller.config().toggleSprint));
        register(DROP = new ControllerBinding<>(controller, GamepadBinds.DPAD_DOWN, new ResourceLocation("controlify", "drop"), options.keyDrop, () -> false));
        register(NEXT_SLOT = new ControllerBinding<>(controller, GamepadBinds.RIGHT_BUMPER, new ResourceLocation("controlify", "next_slot")));
        register(PREV_SLOT = new ControllerBinding<>(controller, GamepadBinds.LEFT_BUMPER, new ResourceLocation("controlify", "prev_slot")));
        register(PAUSE = new ControllerBinding<>(controller, GamepadBinds.START, new ResourceLocation("controlify", "pause")));
        register(INVENTORY = new ControllerBinding<>(controller, GamepadBinds.Y_BUTTON, new ResourceLocation("controlify", "inventory"), options.keyInventory, () -> false));
        register(CHANGE_PERSPECTIVE = new ControllerBinding<>(controller, GamepadBinds.BACK, new ResourceLocation("controlify", "change_perspective"), options.keyTogglePerspective, () -> false));
        register(SWAP_HANDS = new ControllerBinding<>(controller, GamepadBinds.X_BUTTON, new ResourceLocation("controlify", "swap_hands"), options.keySwapOffhand, () -> false));
        register(OPEN_CHAT = new ControllerBinding<>(controller, GamepadBinds.DPAD_UP, new ResourceLocation("controlify", "open_chat"), options.keyChat, () -> false));
        register(GUI_PRESS = new ControllerBinding<>(controller, GamepadBinds.A_BUTTON, new ResourceLocation("controlify", "gui_press")));
        register(GUI_BACK = new ControllerBinding<>(controller, GamepadBinds.B_BUTTON, new ResourceLocation("controlify", "gui_back")));
        register(GUI_NEXT_TAB = new ControllerBinding<>(controller, GamepadBinds.RIGHT_BUMPER, new ResourceLocation("controlify", "gui_next_tab")));
        register(GUI_PREV_TAB = new ControllerBinding<>(controller, GamepadBinds.LEFT_BUMPER, new ResourceLocation("controlify", "gui_prev_tab")));
        register(PICK_BLOCK = new ControllerBinding<>(controller, GamepadBinds.DPAD_LEFT, new ResourceLocation("controlify", "pick_block"), options.keyPickItem, () -> false));
        register(TOGGLE_HUD_VISIBILITY = new ControllerBinding<>(controller, new EmptyBind<>(), new ResourceLocation("controlify", "toggle_hud_visibility")));
        register(SHOW_PLAYER_LIST = new ControllerBinding<>(controller, GamepadBinds.DPAD_RIGHT, new ResourceLocation("controlify", "show_player_list"), options.keyPlayerList, () -> false));
        register(VMOUSE_MOVE_UP = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_FORWARD, new ResourceLocation("controlify", "vmouse_move_up")));
        register(VMOUSE_MOVE_DOWN = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_BACKWARD, new ResourceLocation("controlify", "vmouse_move_down")));
        register(VMOUSE_MOVE_LEFT = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_LEFT, new ResourceLocation("controlify", "vmouse_move_left")));
        register(VMOUSE_MOVE_RIGHT = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_RIGHT, new ResourceLocation("controlify", "vmouse_move_right")));
        register(VMOUSE_LCLICK = new ControllerBinding<>(controller, GamepadBinds.A_BUTTON, new ResourceLocation("controlify", "vmouse_lclick")));
        register(VMOUSE_RCLICK = new ControllerBinding<>(controller, GamepadBinds.X_BUTTON, new ResourceLocation("controlify", "vmouse_rclick")));
        register(VMOUSE_SHIFT_CLICK = new ControllerBinding<>(controller, GamepadBinds.Y_BUTTON, new ResourceLocation("controlify", "vmouse_shift_click")));
        register(VMOUSE_SCROLL_UP = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_FORWARD, new ResourceLocation("controlify", "vmouse_scroll_up")));
        register(VMOUSE_SCROLL_DOWN = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_BACKWARD, new ResourceLocation("controlify", "vmouse_scroll_down")));
        register(VMOUSE_ESCAPE = new ControllerBinding<>(controller, GamepadBinds.B_BUTTON, new ResourceLocation("controlify", "vmouse_escape")));
        register(VMOUSE_SHIFT = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_PRESS, new ResourceLocation("controlify", "vmouse_shift")));
        register(VMOUSE_TOGGLE = new ControllerBinding<>(controller, GamepadBinds.BACK, new ResourceLocation("controlify", "vmouse_toggle")));
        register(GUI_NAVI_UP = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_FORWARD, new ResourceLocation("controlify", "gui_navi_up")));
        register(GUI_NAVI_DOWN = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_BACKWARD, new ResourceLocation("controlify", "gui_navi_down")));
        register(GUI_NAVI_LEFT = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_LEFT, new ResourceLocation("controlify", "gui_navi_left")));
        register(GUI_NAVI_RIGHT = new ControllerBinding<>(controller, GamepadBinds.LEFT_STICK_RIGHT, new ResourceLocation("controlify", "gui_navi_right")));
        register(CYCLE_OPT_FORWARD = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_RIGHT, new ResourceLocation("controlify", "cycle_opt_forward")));
        register(CYCLE_OPT_BACKWARD = new ControllerBinding<>(controller, GamepadBinds.RIGHT_STICK_LEFT, new ResourceLocation("controlify", "cycle_opt_backward")));

        ControlifyEvents.CONTROLLER_BIND_REGISTRY.invoker().onRegisterControllerBinds(this, controller);

        ControlifyEvents.CONTROLLER_STATE_UPDATED.register(this::onControllerUpdate);
        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> KeyMapping.releaseAll());
    }

    public BindingSupplier<T> register(ControllerBinding<T> binding) {
        registry.put(binding.id(), binding);
        return controller -> controller.bindings().get(binding.id());
    }

    public ControllerBinding<T> get(ResourceLocation id) {
        return registry.get(id);
    }

    public Map<ResourceLocation, ControllerBinding<T>> registry() {
        return Collections.unmodifiableMap(registry);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (var binding : registry().values()) {
            json.add(binding.id().toString(), binding.currentBind().toJson());
        }
        return json;
    }

    public void fromJson(JsonObject json) {
        for (var binding : registry().values()) {
            var bind = json.get(binding.id().toString()).getAsJsonObject();
            if (bind == null) {
                Controlify.LOGGER.warn("Unknown control: " + binding.id() + " in config file. Skipping!");
                continue;
            }
            binding.setCurrentBind(IBind.fromJson(bind, controller));
        }
    }

    public void onControllerUpdate(Controller<?, ?> controller) {
        if (controller != this.controller) return;

        imitateVanillaClick();
    }

    private void imitateVanillaClick() {
        ControllerBinding.clearPressedBinds(controller);

        if (Controlify.instance().currentInputMode() != InputMode.CONTROLLER)
            return;
        if (Minecraft.getInstance().screen != null && !Minecraft.getInstance().screen.passEvents)
            return;

        for (var binding : registry().values()) {
            var override = binding.override();
            if (override == null) continue;

            var accessor = (KeyMappingAccessor) override.keyMapping();
            var vanillaKeyCode = accessor.getKey();

            if (override.toggleable().getAsBoolean()) {
                if (binding.justPressed()) {
                    // must set field directly to avoid ToggleKeyMapping breaking things
                    accessor.setIsDown(!accessor.getIsDown());
                }
            } else {
                KeyMapping.set(vanillaKeyCode, binding.held());
            }
            if (binding.justPressed()) KeyMapping.click(vanillaKeyCode);
        }
    }
}
