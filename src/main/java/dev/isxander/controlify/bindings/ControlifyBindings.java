package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.input.action.ActionAccessor;
import dev.isxander.controlify.input.action.ActionHandle;
import dev.isxander.controlify.input.action.ActionSpecBuilder;
import dev.isxander.controlify.input.action.ActionSpecRegistry;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ControlifyBindings {
    public static final Component MOVEMENT_CATEGORY = Component.translatable("key.categories.movement");
    public static final Component GAMEPLAY_CATEGORY = Component.translatable("key.categories.gameplay");
    public static final Component INVENTORY_CATEGORY = Component.translatable("key.categories.inventory");
    public static final Component CREATIVE_CATEGORY = Component.translatable("key.categories.creative");
    public static final Component MISC_CATEGORY = Component.translatable("key.categories.misc");
    public static final Component DEBUG_CATEGORY = Component.translatable("controlify.binding_category.debug");
    public static final Component GUI_CATEGORY = Component.translatable("controlify.binding_category.gui");
    public static final Component RADIAL_CATEGORY = Component.translatable("controlify.gui.radial_menu");
    public static final Component VMOUSE_CATEGORY = Component.translatable("controlify.binding_category.vmouse");

    // Movement - continuous axes
    public static final ActionAccessor<ActionHandle.Continuous> WALK_FORWARD = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("walk_forward"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Continuous> WALK_BACKWARD = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("walk_backward"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Continuous> WALK_LEFT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("strafe_left"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Continuous> WALK_RIGHT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("strafe_right"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));

    public static final ActionAccessor<ActionHandle.Continuous> LOOK_UP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("look_up"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Continuous> LOOK_DOWN = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("look_down"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Continuous> LOOK_LEFT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("look_left"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Continuous> LOOK_RIGHT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("look_right"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));

    // Gyro button (digital latch)
    public static final ActionAccessor<ActionHandle.Latch> GYRO_BUTTON = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gyro_button"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));

    // Gameplay - digital buttons / pulses
    public static final ActionAccessor<ActionHandle.Latch> JUMP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("jump"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Latch> SPRINT_HOLD = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("sprint_hold"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Latch> SPRINT_TOGGLE = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("sprint_toggle"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Latch> SNEAK_HOLD = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("sneak_hold"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Latch> SNEAK_TOGGLE = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("sneak_toggle"))
            .category(MOVEMENT_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Latch> ATTACK = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("attack"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Latch> USE = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("use"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> DROP_INGAME = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("drop"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> DROP_STACK = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("drop_stack"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> PAUSE = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("pause"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id(), BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Pulse> CHANGE_PERSPECTIVE = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("change_perspective"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> SWAP_HANDS = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("swap_hands"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> NEXT_SLOT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("next_slot"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> PREV_SLOT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("prev_slot"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> HOTBAR_SLOT_SELECT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("hotbar_item_select_radial"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id(), BindContext.RADIAL_MENU.id()));
    public static final ActionAccessor<ActionHandle.Pulse> GAME_MODE_SWITCHER = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("game_mode_switcher"))
            .category(GAMEPLAY_CATEGORY)
            .context(BindContext.IN_GAME.id(), BindContext.RADIAL_MENU.id()));

    // Inventory
    public static final ActionAccessor<ActionHandle.Pulse> INVENTORY = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("inventory"))
            .category(INVENTORY_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Latch> INV_SELECT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("inv_select"))
            .category(INVENTORY_CATEGORY)
            .context(BindContext.CONTAINER.id()));
    public static final ActionAccessor<ActionHandle.Pulse> INV_QUICK_MOVE = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("inv_quick_move"))
            .category(INVENTORY_CATEGORY)
            .context(BindContext.CONTAINER.id()));
    public static final ActionAccessor<ActionHandle.Pulse> INV_TAKE_HALF = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("inv_take_half"))
            .category(INVENTORY_CATEGORY)
            .context(BindContext.CONTAINER.id()));
    public static final ActionAccessor<ActionHandle.Pulse> DROP_INVENTORY = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
        .id(CUtil.rl("drop_inventory"))
        .category(INVENTORY_CATEGORY)
        .context(BindContext.CONTAINER.id(), BindContext.REGULAR_SCREEN.id()));

    // Creative
    public static final ActionAccessor<ActionHandle.Pulse> PICK_BLOCK = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("pick_block"))
            .category(CREATIVE_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> PICK_BLOCK_NBT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("pick_block_nbt"))
            .category(CREATIVE_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> HOTBAR_LOAD_RADIAL = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("hotbar_load_radial"))
            .category(CREATIVE_CATEGORY)
            .context(BindContext.IN_GAME.id(), BindContext.RADIAL_MENU.id()));
    public static final ActionAccessor<ActionHandle.Pulse> HOTBAR_SAVE_RADIAL = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("hotbar_save_radial"))
            .category(CREATIVE_CATEGORY)
            .context(BindContext.IN_GAME.id(), BindContext.RADIAL_MENU.id()));

    // Misc
    public static final ActionAccessor<ActionHandle.Pulse> OPEN_CHAT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("open_chat"))
            .category(MISC_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> TOGGLE_HUD_VISIBILITY = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("toggle_hud_visibility"))
            .category(MISC_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Latch> SHOW_PLAYER_LIST = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("show_player_list"))
            .category(MISC_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> TAKE_SCREENSHOT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("take_screenshot"))
            .category(MISC_CATEGORY)
            .context(BindContext.IN_GAME.id()));

    // Debug
    public static final ActionAccessor<ActionHandle.Pulse> DEBUG_RADIAL = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("debug_radial"))
            .category(DEBUG_CATEGORY)
            .context(BindContext.IN_GAME.id(), BindContext.RADIAL_MENU.id()));
    public static final ActionAccessor<ActionHandle.Pulse> TOGGLE_DEBUG_MENU = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("toggle_debug_menu"))
            .category(DEBUG_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> TOGGLE_DEBUG_MENU_FPS = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("toggle_debug_menu_fps"))
            .category(DEBUG_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> TOGGLE_DEBUG_MENU_NET = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("toggle_debug_menu_net"))
            .category(DEBUG_CATEGORY)
            .context(BindContext.IN_GAME.id()));
    public static final ActionAccessor<ActionHandle.Pulse> TOGGLE_DEBUG_MENU_PROF = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("toggle_debug_menu_prof"))
            .category(DEBUG_CATEGORY)
            .context(BindContext.IN_GAME.id()));

    // GUI
    public static final ActionAccessor<ActionHandle.Latch> GUI_PRESS = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_press"))
            .category(GUI_CATEGORY)
            .context(BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Pulse> GUI_BACK = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("gui_back"))
            .category(GUI_CATEGORY)
            .context(BindContext.ANY_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Pulse> GUI_NEXT_TAB = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("gui_next_tab"))
            .category(GUI_CATEGORY)
            .context(BindContext.ANY_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Pulse> GUI_PREV_TAB = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("gui_prev_tab"))
            .category(GUI_CATEGORY)
            .context(BindContext.ANY_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_ABSTRACT_ACTION_1 = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_abstract_action_1"))
            .category(GUI_CATEGORY)
            .context(BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_ABSTRACT_ACTION_2 = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_abstract_action_2"))
            .category(GUI_CATEGORY)
            .context(BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_ABSTRACT_ACTION_3 = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_abstract_action_3"))
            .category(GUI_CATEGORY)
            .context(BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_NAVI_UP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_navi_up"))
            .category(GUI_CATEGORY)
            .context(BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_NAVI_DOWN = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_navi_down"))
            .category(GUI_CATEGORY)
            .context(BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_NAVI_LEFT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_navi_left"))
            .category(GUI_CATEGORY)
            .context(BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_NAVI_RIGHT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_navi_right"))
            .category(GUI_CATEGORY)
            .context(BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_SECONDARY_NAVI_UP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_secondary_navi_up"))
            .category(GUI_CATEGORY)
            .context(BindContext.CONTAINER.id(), BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_SECONDARY_NAVI_DOWN = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_secondary_navi_down"))
            .category(GUI_CATEGORY)
            .context(BindContext.CONTAINER.id(), BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_SECONDARY_NAVI_LEFT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_secondary_navi_left"))
            .category(GUI_CATEGORY)
            .context(BindContext.CONTAINER.id(), BindContext.REGULAR_SCREEN.id()));
    public static final ActionAccessor<ActionHandle.Latch> GUI_SECONDARY_NAVI_RIGHT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("gui_secondary_navi_right"))
            .category(GUI_CATEGORY)
            .context(BindContext.CONTAINER.id(), BindContext.REGULAR_SCREEN.id()));
    @Deprecated
    public static final ActionAccessor<ActionHandle.Latch> CYCLE_OPT_FORWARD = GUI_SECONDARY_NAVI_RIGHT;
    @Deprecated
    public static final ActionAccessor<ActionHandle.Latch> CYCLE_OPT_BACKWARD = GUI_SECONDARY_NAVI_LEFT;

    // Radial Menu
    public static final ActionAccessor<ActionHandle.Latch> RADIAL_MENU = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("radial_menu"))
            .category(RADIAL_CATEGORY)
            .context(BindContext.IN_GAME.id(), BindContext.RADIAL_MENU.id()));
    public static final ActionAccessor<ActionHandle.Continuous> RADIAL_AXIS_UP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("radial_axis_up"))
            .category(RADIAL_CATEGORY)
            .context(BindContext.RADIAL_MENU.id()));
    public static final ActionAccessor<ActionHandle.Continuous> RADIAL_AXIS_DOWN = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("radial_axis_down"))
            .category(RADIAL_CATEGORY)
            .context(BindContext.RADIAL_MENU.id()));
    public static final ActionAccessor<ActionHandle.Continuous> RADIAL_AXIS_LEFT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("radial_axis_left"))
            .category(RADIAL_CATEGORY)
            .context(BindContext.RADIAL_MENU.id()));
    public static final ActionAccessor<ActionHandle.Continuous> RADIAL_AXIS_RIGHT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("radial_axis_right"))
            .category(RADIAL_CATEGORY)
            .context(BindContext.RADIAL_MENU.id()));

    // Virtual Mouse
    public static final ActionAccessor<ActionHandle.Continuous> VMOUSE_MOVE_UP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("vmouse_move_up"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Continuous> VMOUSE_MOVE_DOWN = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("vmouse_move_down"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Continuous> VMOUSE_MOVE_LEFT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("vmouse_move_left"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Continuous> VMOUSE_MOVE_RIGHT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("vmouse_move_right"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Latch> VMOUSE_SNAP_UP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("vmouse_snap_up"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Latch> VMOUSE_SNAP_DOWN = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("vmouse_snap_down"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Latch> VMOUSE_SNAP_LEFT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("vmouse_snap_left"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Latch> VMOUSE_SNAP_RIGHT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("vmouse_snap_right"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Latch> VMOUSE_LCLICK = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("vmouse_lclick"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Latch> VMOUSE_RCLICK = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("vmouse_rclick"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Latch> VMOUSE_SHIFT_CLICK = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("vmouse_shift_click"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Continuous> VMOUSE_SCROLL_DOWN = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("vmouse_scroll_down"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Continuous> VMOUSE_SCROLL_UP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.continuous()
            .id(CUtil.rl("vmouse_scroll_up"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Latch> VMOUSE_SHIFT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.latch()
            .id(CUtil.rl("vmouse_shift"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Pulse> VMOUSE_PAGE_NEXT = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("vmouse_page_next"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Pulse> VMOUSE_PAGE_PREV = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("vmouse_page_prev"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Pulse> VMOUSE_PAGE_DOWN = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("vmouse_page_down"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Pulse> VMOUSE_PAGE_UP = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("vmouse_page_up"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_COMPAT.id(), BindContext.V_MOUSE_CURSOR.id()));
    public static final ActionAccessor<ActionHandle.Pulse> VMOUSE_TOGGLE = ActionSpecRegistry.REGISTRY.register(ActionSpecBuilder.pulse()
            .id(CUtil.rl("vmouse_toggle"))
            .category(VMOUSE_CATEGORY)
            .context(BindContext.V_MOUSE_CURSOR.id(), BindContext.V_MOUSE_COMPAT.id(), BindContext.ANY_SCREEN.id()));

    private static final Map<KeyMapping, InputBindingSupplier> MODDED_BINDS = new LinkedHashMap<>();

    public static void registerModdedBindings() {
        for (KeyMapping keyMapping : PlatformClientUtil.getModdedKeyMappings()) {
            if (!ControlifyBindApi.get().getKeyCorrelation(keyMapping).isEmpty())
                continue;

            try {
                var idPath = keyMapping.getName()
                        .toLowerCase()
                        .replaceAll("[^a-z0-9/._-]", "_")
                        .trim();

                var identifier = ResourceLocation.fromNamespaceAndPath("fabric-key-binding-api-v1", idPath);

                InputBindingSupplier binding = ControlifyBindApi.get().registerBinding(builder -> builder
                        .id(identifier)
                        .name(Component.translatable(keyMapping.getName()))
                        .description(Component.translatable("controlify.custom_binding.vanilla_description").withStyle(ChatFormatting.GRAY))
                        .category(Component.translatable(keyMapping.getCategory()))
                        .radialCandidate(RadialIcons.FABRIC_ICON)
                        .allowedContexts(BindContext.IN_GAME)
                        .keyEmulation(keyMapping));

                MODDED_BINDS.put(keyMapping, binding);
            } catch (Exception e) {
                CUtil.LOGGER.error("Failed to automatically register modded keybind: {}", keyMapping.getName(), e);
            }
        }
    }

    private ControlifyBindings() {
    }
}
