package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.ControlifyBindingsApi;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.bind.ControllerBindingBuilder;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadLike;
import dev.isxander.controlify.mixins.compat.fapi.KeyBindingRegistryImplAccessor;
import dev.isxander.controlify.mixins.feature.bind.KeyMappingAccessor;
import dev.isxander.controlify.mixins.feature.bind.ToggleKeyMappingAccessor;
import dev.isxander.controlify.utils.Log;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class ControllerBindings<T extends ControllerState> {
    private static final Map<ResourceLocation, Function<ControllerBindings<?>, ControllerBinding>> CUSTOM_BINDS = new LinkedHashMap<>();
    private static final Set<KeyMapping> EXCLUDED_VANILLA_BINDS = new HashSet<>();
    private static boolean lockRegistry = false;

    public static final Component MOVEMENT_CATEGORY = Component.translatable("key.categories.movement");
    public static final Component GAMEPLAY_CATEGORY = Component.translatable("key.categories.gameplay");
    public static final Component INVENTORY_CATEGORY = Component.translatable("key.categories.inventory");
    public static final Component CREATIVE_CATEGORY = Component.translatable("key.categories.creative");
    public static final Component VMOUSE_CATEGORY = Component.translatable("controlify.binding_category.vmouse");
    public static final Component GUI_CATEGORY = Component.translatable("controlify.binding_category.gui");
    public static final Component MISC_CATEGORY = Component.translatable("key.categories.misc");
    public static final Component RADIAL_CATEGORY = Component.translatable("controlify.gui.radial_menu");

    public final ControllerBinding
            WALK_FORWARD, WALK_BACKWARD, WALK_LEFT, WALK_RIGHT,
            LOOK_UP, LOOK_DOWN, LOOK_LEFT, LOOK_RIGHT,
            GAMEPAD_GYRO_BUTTON,
            JUMP, SNEAK,
            ATTACK, USE,
            SPRINT,
            DROP_INGAME, DROP_STACK, DROP_INVENTORY,
            NEXT_SLOT, PREV_SLOT,
            PAUSE,
            INVENTORY,
            CHANGE_PERSPECTIVE,
            SWAP_HANDS,
            OPEN_CHAT,
            INV_SELECT, INV_QUICK_MOVE, INV_TAKE_HALF,
            GUI_PRESS, GUI_BACK,
            GUI_NEXT_TAB, GUI_PREV_TAB,
            GUI_ABSTRACT_ACTION_1, GUI_ABSTRACT_ACTION_2,
            PICK_BLOCK, PICK_BLOCK_NBT,
            TOGGLE_HUD_VISIBILITY,
            SHOW_PLAYER_LIST,
            TAKE_SCREENSHOT,
            TOGGLE_DEBUG_MENU,
            RADIAL_MENU, RADIAL_AXIS_UP, RADIAL_AXIS_DOWN, RADIAL_AXIS_LEFT, RADIAL_AXIS_RIGHT,
            VMOUSE_MOVE_UP, VMOUSE_MOVE_DOWN, VMOUSE_MOVE_LEFT, VMOUSE_MOVE_RIGHT,
            VMOUSE_LCLICK, VMOUSE_RCLICK, VMOUSE_SHIFT_CLICK,
            VMOUSE_SNAP_UP, VMOUSE_SNAP_DOWN, VMOUSE_SNAP_LEFT, VMOUSE_SNAP_RIGHT,
            VMOUSE_SCROLL_UP, VMOUSE_SCROLL_DOWN,
            VMOUSE_SHIFT,
            VMOUSE_TOGGLE,
            GUI_NAVI_UP, GUI_NAVI_DOWN, GUI_NAVI_LEFT, GUI_NAVI_RIGHT,
            CYCLE_OPT_FORWARD, CYCLE_OPT_BACKWARD;

    private final Map<ResourceLocation, ControllerBinding> registry = new Object2ObjectLinkedOpenHashMap<>();

    private final Controller<T, ?> controller;

    public ControllerBindings(Controller<T, ?> controller) {
        this.controller = controller;
        var options = Minecraft.getInstance().options;

        register(WALK_FORWARD = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "walk_forward")
                .defaultBind(GamepadBinds.LEFT_STICK_FORWARD)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(WALK_BACKWARD = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "walk_backward")
                .defaultBind(GamepadBinds.LEFT_STICK_BACKWARD)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(WALK_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "strafe_left")
                .defaultBind(GamepadBinds.LEFT_STICK_LEFT)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(WALK_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "strafe_right")
                .defaultBind(GamepadBinds.LEFT_STICK_RIGHT)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(LOOK_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "look_up")
                .defaultBind(GamepadBinds.RIGHT_STICK_FORWARD)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(LOOK_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "look_down")
                .defaultBind(GamepadBinds.RIGHT_STICK_BACKWARD)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(LOOK_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "look_left")
                .defaultBind(GamepadBinds.RIGHT_STICK_LEFT)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(LOOK_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "look_right")
                .defaultBind(GamepadBinds.RIGHT_STICK_RIGHT)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        if (controller instanceof GamepadLike<?> gamepad && gamepad.supportsGyro()) {
            register(GAMEPAD_GYRO_BUTTON = ControllerBindingBuilder.create(controller)
                    .identifier("controlify", "gamepad_gyro_button")
                    .defaultBind(new EmptyBind<>())
                    .category(MOVEMENT_CATEGORY)
                    .context(BindContexts.INGAME)
                    .build());
        } else {
            GAMEPAD_GYRO_BUTTON = null;
        }
        register(JUMP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "jump")
                .defaultBind(GamepadBinds.A_BUTTON)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getEffect(MobEffects.JUMP))
                .build());
        register(SPRINT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "sprint")
                .defaultBind(GamepadBinds.LEFT_STICK_PRESS)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .vanillaOverride(options.keySprint, () -> controller.config().toggleSprint)
                .build());
        register(SNEAK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "sneak")
                .defaultBind(GamepadBinds.RIGHT_STICK_PRESS)
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(ATTACK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "attack")
                .defaultBind(GamepadBinds.RIGHT_TRIGGER)
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .vanillaOverride(options.keyAttack, () -> false)
                .build());
        register(USE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "use")
                .defaultBind(GamepadBinds.LEFT_TRIGGER)
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .vanillaOverride(options.keyUse, () -> false)
                .build());
        register(DROP_INGAME = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "drop")
                .defaultBind(GamepadBinds.DPAD_DOWN)
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME, BindContexts.INVENTORY)
                .radialCandidate(RadialIcons.getItem(Items.BARRIER))
                .build());
        register(DROP_STACK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "drop_stack")
                .defaultBind(new EmptyBind<>())
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.TNT))
                .build());
        register(DROP_INVENTORY = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "drop_inventory")
                .defaultBind(GamepadBinds.Y_BUTTON)
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INVENTORY)
                .build());
        register(NEXT_SLOT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "next_slot")
                .defaultBind(GamepadBinds.RIGHT_BUMPER)
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(PREV_SLOT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "prev_slot")
                .defaultBind(GamepadBinds.LEFT_BUMPER)
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(PAUSE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "pause")
                .defaultBind(GamepadBinds.START)
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.STRUCTURE_VOID))
                .build());
        register(INVENTORY = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "inventory")
                .defaultBind(GamepadBinds.Y_BUTTON)
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.CHEST))
                .build());
        register(CHANGE_PERSPECTIVE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "change_perspective")
                .defaultBind(GamepadBinds.BACK)
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.PAINTING))
                .build());
        register(SWAP_HANDS = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "swap_hands")
                .defaultBind(GamepadBinds.X_BUTTON)
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.BONE))
                .build());
        register(OPEN_CHAT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "open_chat")
                .defaultBind(GamepadBinds.DPAD_UP)
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.WRITABLE_BOOK))
                .vanillaOverride(options.keyChat, () -> false)
                .build());
        register(GUI_PRESS = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_press")
                .defaultBind(GamepadBinds.A_BUTTON)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_BACK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_back")
                .defaultBind(GamepadBinds.B_BUTTON)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI, BindContexts.GUI_VMOUSE)
                .build());
        register(GUI_NEXT_TAB = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_next_tab")
                .defaultBind(GamepadBinds.RIGHT_BUMPER)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI, BindContexts.GUI_VMOUSE)
                .build());
        register(GUI_PREV_TAB = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_prev_tab")
                .defaultBind(GamepadBinds.LEFT_BUMPER)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI, BindContexts.GUI_VMOUSE)
                .build());
        register(GUI_ABSTRACT_ACTION_1 = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_abstract_action_1")
                .defaultBind(GamepadBinds.X_BUTTON)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_ABSTRACT_ACTION_2 = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_abstract_action_2")
                .defaultBind(GamepadBinds.Y_BUTTON)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(INV_SELECT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "inv_select")
                .defaultBind(GamepadBinds.A_BUTTON)
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INVENTORY)
                .build());
        register(INV_QUICK_MOVE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "inv_quick_move")
                .defaultBind(GamepadBinds.Y_BUTTON)
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INVENTORY)
                .build());
        register(INV_TAKE_HALF = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "inv_take_half")
                .defaultBind(GamepadBinds.X_BUTTON)
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INVENTORY)
                .build());
        register(PICK_BLOCK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "pick_block")
                .defaultBind(GamepadBinds.DPAD_LEFT)
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.STICK))
                .build());
        register(PICK_BLOCK_NBT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "pick_block_nbt")
                .defaultBind(new EmptyBind<>())
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK))
                .build());
        register(TOGGLE_HUD_VISIBILITY = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "toggle_hud_visibility")
                .defaultBind(new EmptyBind<>())
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getEffect(MobEffects.INVISIBILITY))
                .build());
        register(SHOW_PLAYER_LIST = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "show_player_list")
                .defaultBind(new EmptyBind<>())
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.PLAYER_HEAD))
                .build());
        register(TAKE_SCREENSHOT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "take_screenshot")
                .defaultBind(new EmptyBind<>())
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.SPYGLASS))
                .build());
        register(TOGGLE_DEBUG_MENU = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "toggle_debug_menu")
                .defaultBind(new EmptyBind<>())
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK))
                .build());
        register(RADIAL_MENU = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_menu")
                .defaultBind(GamepadBinds.DPAD_RIGHT)
                .category(RADIAL_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(RADIAL_AXIS_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_axis_up")
                .defaultBind(GamepadBinds.RIGHT_STICK_FORWARD)
                .category(RADIAL_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(RADIAL_AXIS_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_axis_down")
                .defaultBind(GamepadBinds.RIGHT_STICK_BACKWARD)
                .category(RADIAL_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(RADIAL_AXIS_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_axis_left")
                .defaultBind(GamepadBinds.RIGHT_STICK_LEFT)
                .category(RADIAL_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(RADIAL_AXIS_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_axis_right")
                .defaultBind(GamepadBinds.RIGHT_STICK_RIGHT)
                .category(RADIAL_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(VMOUSE_MOVE_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_move_up")
                .defaultBind(GamepadBinds.LEFT_STICK_FORWARD)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE_CURSOR_ONLY)
                .build());
        register(VMOUSE_MOVE_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_move_down")
                .defaultBind(GamepadBinds.LEFT_STICK_BACKWARD)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE_CURSOR_ONLY)
                .build());
        register(VMOUSE_MOVE_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_move_left")
                .defaultBind(GamepadBinds.LEFT_STICK_LEFT)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE_CURSOR_ONLY)
                .build());
        register(VMOUSE_MOVE_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_move_right")
                .defaultBind(GamepadBinds.LEFT_STICK_RIGHT)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE_CURSOR_ONLY)
                .build());
        register(VMOUSE_LCLICK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_lclick")
                .defaultBind(GamepadBinds.A_BUTTON)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_RCLICK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_rclick")
                .defaultBind(GamepadBinds.X_BUTTON)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SHIFT_CLICK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_shift_click")
                .defaultBind(GamepadBinds.Y_BUTTON)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SNAP_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_snap_up")
                .defaultBind(GamepadBinds.DPAD_UP)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SNAP_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_snap_down")
                .defaultBind(GamepadBinds.DPAD_DOWN)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SNAP_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_snap_left")
                .defaultBind(GamepadBinds.DPAD_LEFT)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SNAP_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_snap_right")
                .defaultBind(GamepadBinds.DPAD_RIGHT)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SCROLL_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_scroll_up")
                .defaultBind(GamepadBinds.RIGHT_STICK_FORWARD)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SCROLL_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_scroll_down")
                .defaultBind(GamepadBinds.RIGHT_STICK_BACKWARD)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SHIFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_shift")
                .defaultBind(GamepadBinds.LEFT_STICK_PRESS)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_TOGGLE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_toggle")
                .defaultBind(GamepadBinds.BACK)
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE, BindContexts.GUI)
                .build());
        register(GUI_NAVI_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_navi_up")
                .defaultBind(GamepadBinds.LEFT_STICK_FORWARD)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_NAVI_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_navi_down")
                .defaultBind(GamepadBinds.LEFT_STICK_BACKWARD)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_NAVI_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_navi_left")
                .defaultBind(GamepadBinds.LEFT_STICK_LEFT)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_NAVI_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_navi_right")
                .defaultBind(GamepadBinds.LEFT_STICK_RIGHT)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(CYCLE_OPT_FORWARD = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "cycle_opt_forward")
                .defaultBind(GamepadBinds.RIGHT_STICK_RIGHT)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(CYCLE_OPT_BACKWARD = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "cycle_opt_backward")
                .defaultBind(GamepadBinds.RIGHT_STICK_LEFT)
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());

        for (var constructor : CUSTOM_BINDS.values()) {
            register(constructor.apply(this));
        }

        registerModdedKeybinds();

        ControlifyEvents.CONTROLLER_STATE_UPDATE.register(ctrl -> {
            if (ctrl == this.controller) {
                this.imitateVanillaClick();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            registry().values().forEach(ControllerBinding::tick);
        });

        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> KeyMapping.releaseAll());
    }

    public ControllerBinding register(ControllerBinding binding) {
        registry.put(binding.id(), binding);

        if (binding.override() != null) {
            ((KeyMappingOverrideHolder) binding.override().keyMapping()).controlify$addOverride(binding);
        }

        return binding;
    }

    private ControllerBinding create(UnaryOperator<ControllerBindingBuilder<?>> builder) {
        return builder.apply(ControllerBindingBuilder.create(controller)).build();
    }

    @Deprecated
    private ControllerBinding create(GamepadBinds bind, ResourceLocation id) {
        return ControllerBindingBuilder.create(controller)
                .identifier(id)
                .defaultBind(bind)
                .build();
    }

    @Deprecated
    private ControllerBinding create(GamepadBinds bind, ResourceLocation id, KeyMapping override, BooleanSupplier toggleOverride) {
        return ControllerBindingBuilder.create(controller)
                .identifier(id)
                .defaultBind(bind)
                .vanillaOverride(override, toggleOverride)
                .build();
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
            json.add(binding.id().toString(), binding.toJson());
        }
        return json;
    }

    public boolean fromJson(JsonObject json) {
        boolean clean = true;
        for (var binding : registry().values()) {
            if (!json.has(binding.id().toString())) {
                Log.LOGGER.warn("Missing binding: " + binding.id() + " in config file. Skipping!");
                clean = false;
                continue;
            }

            var bind = json.get(binding.id().toString()).getAsJsonObject();
            if (bind == null) {
                Log.LOGGER.warn("Unknown binding: " + binding.id() + " in config file. Skipping!");
                clean = false;
                continue;
            }
            ((ControllerBindingImpl<T>) binding).setCurrentBind(IBind.fromJson(bind, controller));
        }

        return clean;
    }

    private void registerModdedKeybinds() {
        for (KeyMapping keyMapping : KeyBindingRegistryImplAccessor.getCustomKeys()) {
            if (EXCLUDED_VANILLA_BINDS.contains(keyMapping))
                continue;

            try {
                var idPath = keyMapping.getName()
                        .toLowerCase()
                        .replaceAll("[^a-z0-9/._-]", "_")
                        .trim();

                var identifier = new ResourceLocation("fabric-key-binding-api-v1", idPath);
                BooleanSupplier toggleOverride = () -> false;
                if (keyMapping instanceof ToggleKeyMapping toggleKeyMapping) {
                    toggleOverride = ((ToggleKeyMappingAccessor) toggleKeyMapping).getNeedsToggle();
                }

                ControllerBinding binding = ControllerBindingBuilder.create(controller)
                        .identifier(identifier)
                        .defaultBind(new EmptyBind<>())
                        .name(Component.translatable(keyMapping.getName()))
                        .description(Component.translatable("controlify.custom_binding.vanilla_description").withStyle(ChatFormatting.GRAY))
                        .category(Component.translatable(keyMapping.getCategory()))
                        .radialCandidate(RadialIcons.FABRIC_ICON)
                        .vanillaOverride(keyMapping, toggleOverride)
                        .build();

                register(binding);
            } catch (Exception e) {
                Log.LOGGER.error("Failed to automatically register modded keybind: " + keyMapping.getName(), e);
            }
        }
    }

    private void imitateVanillaClick() {
        ControllerBindingImpl.clearPressedBinds(controller);

        if (!Controlify.instance().currentInputMode().isController())
            return;
        if (Minecraft.getInstance().screen != null)
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
            }
            if (binding.justPressed()) {
                KeyMapping.click(vanillaKeyCode);
            }
        }
    }

    public static void lockRegistry() {
        Api.INSTANCE.lockRegistry();
    }

    public static final class Api implements ControlifyBindingsApi {
        public static final Api INSTANCE = new Api();

        private boolean lockedRegistry = false;

        @Override
        public dev.isxander.controlify.api.bind.BindingSupplier registerBind(ResourceLocation id, UnaryOperator<ControllerBindingBuilder<?>> builder) {
            checkLocked();
            CUSTOM_BINDS.put(id, bindings -> bindings.create(b -> builder.apply(b).identifier(id)));
            return controller -> controller.bindings().get(id);
        }

        @Deprecated
        @Override
        public dev.isxander.controlify.api.bind.BindingSupplier registerBind(GamepadBinds bind, ResourceLocation id) {
            checkLocked();
            CUSTOM_BINDS.put(id, bindings -> bindings.create(bind, id));
            return controller -> controller.bindings().get(id);
        }

        @Deprecated
        @Override
        public dev.isxander.controlify.api.bind.BindingSupplier registerBind(GamepadBinds bind, ResourceLocation id, KeyMapping override, BooleanSupplier toggleOverride) {
            checkLocked();
            CUSTOM_BINDS.put(id, bindings -> bindings.create(bind, id, override, toggleOverride));
            return controller -> controller.bindings().get(id);
        }

        @Override
        public void excludeVanillaBind(KeyMapping... keyMappings) {
            checkLocked();
            EXCLUDED_VANILLA_BINDS.addAll(Arrays.asList(keyMappings));
        }

        @Override
        public void registerRadialIcon(ResourceLocation id, RadialIcon icon) {
            checkLocked();
            RadialIcons.registerIcon(id, icon);
        }

        public void lockRegistry() {
            this.lockedRegistry = true;
        }

        private void checkLocked() {
            Validate.isTrue(!lockedRegistry, "Cannot register new binds after registry is locked! You most likely tried to register a binding too late in Controlify's lifecycle.");
        }
    }
}
