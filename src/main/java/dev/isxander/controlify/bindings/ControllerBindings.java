package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindingsApi;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.bind.ControllerBindingBuilder;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.compat.fapi.KeyBindingRegistryImplAccessor;
import dev.isxander.controlify.mixins.feature.bind.KeyMappingAccessor;
import dev.isxander.controlify.mixins.feature.bind.ToggleKeyMappingAccessor;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
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

public class ControllerBindings {
    private static final Map<ResourceLocation, Function<ControllerBindings, ControllerBinding>> CUSTOM_BINDS = new LinkedHashMap<>();
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
            HOTBAR_ITEM_SELECT_RADIAL,
            HOTBAR_LOAD_RADIAL, HOTBAR_SAVE_RADIAL,
            GUI_PRESS, GUI_BACK,
            GUI_NEXT_TAB, GUI_PREV_TAB,
            GUI_ABSTRACT_ACTION_1, GUI_ABSTRACT_ACTION_2,
            PICK_BLOCK, PICK_BLOCK_NBT,
            TOGGLE_HUD_VISIBILITY,
            SHOW_PLAYER_LIST,
            TAKE_SCREENSHOT,
            TOGGLE_DEBUG_MENU,
            RADIAL_MENU, RADIAL_AXIS_UP, RADIAL_AXIS_DOWN, RADIAL_AXIS_LEFT, RADIAL_AXIS_RIGHT,
            GAME_MODE_SWITCHER,
            VMOUSE_MOVE_UP, VMOUSE_MOVE_DOWN, VMOUSE_MOVE_LEFT, VMOUSE_MOVE_RIGHT,
            VMOUSE_LCLICK, VMOUSE_RCLICK, VMOUSE_SHIFT_CLICK,
            VMOUSE_SNAP_UP, VMOUSE_SNAP_DOWN, VMOUSE_SNAP_LEFT, VMOUSE_SNAP_RIGHT,
            VMOUSE_SCROLL_UP, VMOUSE_SCROLL_DOWN,
            VMOUSE_SHIFT,
            VMOUSE_TOGGLE,
            VMOUSE_PAGE_NEXT, VMOUSE_PAGE_PREV,
            VMOUSE_PAGE_DOWN, VMOUSE_PAGE_UP,
            GUI_NAVI_UP, GUI_NAVI_DOWN, GUI_NAVI_LEFT, GUI_NAVI_RIGHT,
            CYCLE_OPT_FORWARD, CYCLE_OPT_BACKWARD;

    private final Map<ResourceLocation, ControllerBinding> registry = new Object2ObjectLinkedOpenHashMap<>();

    private final ControllerEntity controller;

    public ControllerBindings(ControllerEntity controller) {
        this.controller = controller;
        var options = Minecraft.getInstance().options;

        register(WALK_FORWARD = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "walk_forward")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_UP))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(WALK_BACKWARD = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "walk_backward")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_DOWN))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(WALK_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "strafe_left")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_LEFT))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(WALK_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "strafe_right")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_RIGHT))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(LOOK_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "look_up")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_UP))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(LOOK_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "look_down")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_DOWN))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(LOOK_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "look_left")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_LEFT))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(LOOK_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "look_right")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_RIGHT))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        if (controller.gyro().isPresent()) {
            register(GAMEPAD_GYRO_BUTTON = ControllerBindingBuilder.create(controller)
                    .identifier("controlify", "gyro_button")
                    .defaultBind(new EmptyBind())
                    .category(MOVEMENT_CATEGORY)
                    .context(BindContexts.INGAME)
                    .build());
        } else {
            GAMEPAD_GYRO_BUTTON = null;
        }
        register(JUMP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "jump")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.SOUTH_BUTTON))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getEffect(MobEffects.JUMP))
                .build());
        register(SPRINT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "sprint")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_BUTTON))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .vanillaOverride(options.keySprint, () -> controller.genericConfig().config().toggleSprint)
                .build());
        register(SNEAK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "sneak")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_BUTTON))
                .category(MOVEMENT_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(ATTACK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "attack")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_TRIGGER_AXIS))
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .vanillaOverride(options.keyAttack, () -> false)
                .build());
        register(USE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "use")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_TRIGGER_AXIS))
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .vanillaOverride(options.keyUse, () -> false)
                .build());
        register(DROP_INGAME = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "drop")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.DPAD_DOWN_BUTTON))
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME, BindContexts.INVENTORY)
                .radialCandidate(RadialIcons.getItem(Items.BARRIER))
                .build());
        register(DROP_STACK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "drop_stack")
                .defaultBind(new EmptyBind())
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.TNT))
                .build());
        register(DROP_INVENTORY = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "drop_inventory")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.NORTH_BUTTON))
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INVENTORY)
                .build());
        register(NEXT_SLOT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "next_slot")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_SHOULDER_BUTTON))
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(PREV_SLOT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "prev_slot")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_SHOULDER_BUTTON))
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(PAUSE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "pause")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.START_BUTTON))
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.STRUCTURE_VOID))
                .build());
        register(INVENTORY = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "inventory")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.NORTH_BUTTON))
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.CHEST))
                .build());
        register(CHANGE_PERSPECTIVE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "change_perspective")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.BACK_BUTTON))
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.PAINTING))
                .build());
        register(SWAP_HANDS = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "swap_hands")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.WEST_BUTTON))
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.BONE))
                .build());
        register(OPEN_CHAT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "open_chat")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.DPAD_UP_BUTTON))
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.WRITABLE_BOOK))
                .vanillaOverride(options.keyChat, () -> false)
                .build());
        register(GUI_PRESS = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_press")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.SOUTH_BUTTON))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .hardcodedBind(state -> Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled() ? (state.isButtonDown(GamepadInputs.TOUCHPAD_BUTTON) ? 1f : 0f) : 0f)
                .build());
        register(GUI_BACK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_back")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.EAST_BUTTON))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI, BindContexts.GUI_VMOUSE)
                .build());
        register(GUI_NEXT_TAB = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_next_tab")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_SHOULDER_BUTTON))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI, BindContexts.GUI_VMOUSE)
                .build());
        register(GUI_PREV_TAB = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_prev_tab")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_SHOULDER_BUTTON))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI, BindContexts.GUI_VMOUSE)
                .build());
        register(GUI_ABSTRACT_ACTION_1 = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_abstract_action_1")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.WEST_BUTTON))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_ABSTRACT_ACTION_2 = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_abstract_action_2")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.NORTH_BUTTON))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(INV_SELECT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "inv_select")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.SOUTH_BUTTON))
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INVENTORY)
                .build());
        register(INV_QUICK_MOVE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "inv_quick_move")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.NORTH_BUTTON))
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INVENTORY)
                .build());
        register(INV_TAKE_HALF = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "inv_take_half")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.WEST_BUTTON))
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INVENTORY)
                .build());
        register(HOTBAR_LOAD_RADIAL = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "hotbar_load_radial")
                .defaultBind(new EmptyBind())
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(HOTBAR_SAVE_RADIAL = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "hotbar_save_radial")
                .defaultBind(new EmptyBind())
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(HOTBAR_ITEM_SELECT_RADIAL = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "hotbar_item_select_radial")
                .defaultBind(new EmptyBind())
                .category(INVENTORY_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(PICK_BLOCK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "pick_block")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.DPAD_LEFT_BUTTON))
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.STICK))
                .build());
        register(PICK_BLOCK_NBT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "pick_block_nbt")
                .defaultBind(new EmptyBind())
                .category(GAMEPLAY_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK))
                .build());
        register(TOGGLE_HUD_VISIBILITY = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "toggle_hud_visibility")
                .defaultBind(new EmptyBind())
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getEffect(MobEffects.INVISIBILITY))
                .build());
        register(SHOW_PLAYER_LIST = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "show_player_list")
                .defaultBind(new EmptyBind())
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.PLAYER_HEAD))
                .build());
        register(TAKE_SCREENSHOT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "take_screenshot")
                .defaultBind(new EmptyBind())
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.SPYGLASS))
                .build());
        register(TOGGLE_DEBUG_MENU = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "toggle_debug_menu")
                .defaultBind(new EmptyBind())
                .category(MISC_CATEGORY)
                .context(BindContexts.INGAME)
                .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK))
                .build());
        register(RADIAL_MENU = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_menu")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.DPAD_RIGHT_BUTTON))
                .category(RADIAL_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(RADIAL_AXIS_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_axis_up")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_UP))
                .category(RADIAL_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(RADIAL_AXIS_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_axis_down")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_DOWN))
                .category(RADIAL_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(RADIAL_AXIS_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_axis_left")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_LEFT))
                .category(RADIAL_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(RADIAL_AXIS_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "radial_axis_right")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_RIGHT))
                .category(RADIAL_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GAME_MODE_SWITCHER = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "game_mode_switcher")
                .defaultBind(new EmptyBind())
                .category(RADIAL_CATEGORY)
                .context(BindContexts.INGAME)
                .build());
        register(VMOUSE_MOVE_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_move_up")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_UP))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE_CURSOR_ONLY)
                .build());
        register(VMOUSE_MOVE_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_move_down")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_DOWN))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE_CURSOR_ONLY)
                .build());
        register(VMOUSE_MOVE_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_move_left")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_LEFT))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE_CURSOR_ONLY)
                .build());
        register(VMOUSE_MOVE_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_move_right")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_RIGHT))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE_CURSOR_ONLY)
                .build());
        register(VMOUSE_LCLICK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_lclick")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.SOUTH_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_RCLICK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_rclick")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.WEST_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SHIFT_CLICK = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_shift_click")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.NORTH_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SNAP_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_snap_up")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.DPAD_UP_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SNAP_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_snap_down")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.DPAD_DOWN_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SNAP_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_snap_left")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.DPAD_LEFT_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SNAP_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_snap_right")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.DPAD_RIGHT_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SCROLL_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_scroll_up")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_UP))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SCROLL_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_scroll_down")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_DOWN))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_SHIFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_shift")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_TOGGLE = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_toggle")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.BACK_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE, BindContexts.GUI)
                .build());
        register(VMOUSE_PAGE_NEXT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_page_next")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_SHOULDER_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_PAGE_PREV = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_page_prev")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_SHOULDER_BUTTON))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_PAGE_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_page_down")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_TRIGGER_AXIS))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(VMOUSE_PAGE_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "vmouse_page_up")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_TRIGGER_AXIS))
                .category(VMOUSE_CATEGORY)
                .context(BindContexts.GUI_VMOUSE)
                .build());
        register(GUI_NAVI_UP = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_navi_up")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_UP))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_NAVI_DOWN = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_navi_down")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_DOWN))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_NAVI_LEFT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_navi_left")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_LEFT))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(GUI_NAVI_RIGHT = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "gui_navi_right")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.LEFT_STICK_AXIS_RIGHT))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(CYCLE_OPT_FORWARD = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "cycle_opt_forward")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_RIGHT))
                .category(GUI_CATEGORY)
                .context(BindContexts.GUI)
                .build());
        register(CYCLE_OPT_BACKWARD = ControllerBindingBuilder.create(controller)
                .identifier("controlify", "cycle_opt_backward")
                .defaultBind(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_LEFT))
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
        PlatformClientUtil.registerClientTickEnded(client -> {
            registry().values().forEach(ControllerBinding::tick);
        });

        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> KeyMapping.releaseAll());
    }

    public ControllerBinding register(ControllerBinding binding) {
        registry.put(binding.id(), binding);

        if (binding.override() != null) {
            ((KeyMappingOverrideHolder) binding.override().keyMapping()).controlify$addOverride(() ->
                    ControlifyApi.get().getCurrentController().equals(Optional.of(controller))
                            && Controlify.instance().currentInputMode().isController()
                            && binding.held()
                            && !binding.override().toggleable().getAsBoolean()
            );
        }

        return binding;
    }

    private ControllerBinding create(UnaryOperator<ControllerBindingBuilder> builder) {
        return builder.apply(ControllerBindingBuilder.create(controller)).build();
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
                CUtil.LOGGER.warn("Missing binding: {} in config file. Skipping!", binding.id());
                clean = false;
                continue;
            }

            var bind = json.get(binding.id().toString()).getAsJsonObject();
            if (bind == null) {
                CUtil.LOGGER.warn("Unknown binding: {} in config file. Skipping!", binding.id());
                clean = false;
                continue;
            }
            ((ControllerBindingImpl) binding).setCurrentBind(IBind.fromJson(bind));
        }

        return clean;
    }

    @SuppressWarnings("UnreachableCode")
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
                        .defaultBind(new EmptyBind())
                        .name(Component.translatable(keyMapping.getName()))
                        .description(Component.translatable("controlify.custom_binding.vanilla_description").withStyle(ChatFormatting.GRAY))
                        .category(Component.translatable(keyMapping.getCategory()))
                        .radialCandidate(RadialIcons.FABRIC_ICON)
                        .vanillaOverride(keyMapping, toggleOverride)
                        .build();

                register(binding);
            } catch (Exception e) {
                CUtil.LOGGER.error("Failed to automatically register modded keybind: " + keyMapping.getName(), e);
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
        public dev.isxander.controlify.api.bind.BindingSupplier registerBind(ResourceLocation id, UnaryOperator<ControllerBindingBuilder> builder) {
            checkLocked();
            CUSTOM_BINDS.put(id, bindings -> bindings.create(b -> builder.apply(b).identifier(id)));
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
