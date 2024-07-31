package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ControlifyBindings {
    private static final Options options = Minecraft.getInstance().options;

    private static final Component MOVEMENT_CATEGORY = Component.translatable("key.categories.movement");
    private static final Component GAMEPLAY_CATEGORY = Component.translatable("key.categories.gameplay");
    private static final Component INVENTORY_CATEGORY = Component.translatable("key.categories.inventory");
    private static final Component CREATIVE_CATEGORY = Component.translatable("key.categories.creative");
    private static final Component MISC_CATEGORY = Component.translatable("key.categories.misc");
    private static final Component DEBUG_CATEGORY = Component.translatable("controlify.binding_category.debug");
    private static final Component GUI_CATEGORY = Component.translatable("controlify.binding_category.gui");
    private static final Component RADIAL_CATEGORY = Component.translatable("controlify.gui.radial_menu");
    private static final Component VMOUSE_CATEGORY = Component.translatable("controlify.binding_category.vmouse");

    public static final InputBindingSupplier WALK_FORWARD = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "walk_forward")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));
    public static final InputBindingSupplier WALK_BACKWARD = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "walk_backward")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));
    public static final InputBindingSupplier WALK_LEFT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "strafe_left")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));
    public static final InputBindingSupplier WALK_RIGHT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "strafe_right")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));

    public static final InputBindingSupplier LOOK_UP = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "look_up")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));
    public static final InputBindingSupplier LOOK_DOWN = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "look_down")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));
    public static final InputBindingSupplier LOOK_LEFT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "look_left")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));
    public static final InputBindingSupplier LOOK_RIGHT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "look_right")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));

    public static final InputBindingSupplier GYRO_BUTTON = ControlifyBindApi.get().registerBinding(
            builder -> builder
                    .id("controlify", "gyro_button")
                    .category(MOVEMENT_CATEGORY)
                    .allowedContexts(BindContext.IN_GAME),
            c -> c.gyro().isPresent()
    );

    public static final InputBindingSupplier JUMP = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "jump")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getEffect(MobEffects.JUMP)));
    public static final InputBindingSupplier SPRINT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "sprint")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .keyEmulation(options.keySprint, c -> c.genericConfig().config().toggleSprint));
    public static final InputBindingSupplier SNEAK =  ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "sneak")
            .category(MOVEMENT_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .addKeyCorrelation(options.keyShift));

    public static final InputBindingSupplier ATTACK = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "attack")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .keyEmulation(options.keyAttack));
    public static final InputBindingSupplier USE = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "use")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .keyEmulation(options.keyUse));
    public static final InputBindingSupplier DROP_INGAME = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "drop")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.BARRIER)));
    public static final InputBindingSupplier DROP_STACK = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "drop_stack")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.TNT)));
    public static final InputBindingSupplier PAUSE = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "pause")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.STRUCTURE_VOID)));
    public static final InputBindingSupplier CHANGE_PERSPECTIVE = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "change_perspective")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.PAINTING)));
    public static final InputBindingSupplier SWAP_HANDS = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "swap_hands")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.BONE)));
    public static final InputBindingSupplier NEXT_SLOT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "next_slot")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));
    public static final InputBindingSupplier PREV_SLOT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "prev_slot")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME));
    public static final InputBindingSupplier HOTBAR_SLOT_SELECT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "hotbar_item_select_radial")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME, BindContext.RADIAL_MENU));
    public static final InputBindingSupplier GAME_MODE_SWITCHER = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "game_mode_switcher")
            .category(GAMEPLAY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME, BindContext.RADIAL_MENU));

    public static final InputBindingSupplier INVENTORY = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "inventory")
            .category(INVENTORY_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.CHEST)));
    public static final InputBindingSupplier INV_SELECT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "inv_select")
            .category(INVENTORY_CATEGORY)
            .allowedContexts(BindContext.CONTAINER));
    public static final InputBindingSupplier INV_QUICK_MOVE = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "inv_quick_move")
            .category(INVENTORY_CATEGORY)
            .allowedContexts(BindContext.CONTAINER));
    public static final InputBindingSupplier INV_TAKE_HALF = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "inv_take_half")
            .category(INVENTORY_CATEGORY)
            .allowedContexts(BindContext.CONTAINER));
    public static final InputBindingSupplier DROP_INVENTORY = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "drop_inventory")
            .category(INVENTORY_CATEGORY)
            .allowedContexts(BindContext.CONTAINER));

    public static final InputBindingSupplier PICK_BLOCK = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "pick_block")
            .category(CREATIVE_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.STICK)));
    public static final InputBindingSupplier PICK_BLOCK_NBT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "pick_block_nbt")
            .category(CREATIVE_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK)));
    public static final InputBindingSupplier HOTBAR_LOAD_RADIAL = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "hotbar_load_radial")
            .category(CREATIVE_CATEGORY)
            .allowedContexts(BindContext.IN_GAME, BindContext.RADIAL_MENU));
    public static final InputBindingSupplier HOTBAR_SAVE_RADIAL = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "hotbar_save_radial")
            .category(CREATIVE_CATEGORY)
            .allowedContexts(BindContext.IN_GAME, BindContext.RADIAL_MENU));

    public static final InputBindingSupplier OPEN_CHAT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "open_chat")
            .category(MISC_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.WRITABLE_BOOK))
            .keyEmulation(options.keyChat));
    public static final InputBindingSupplier TOGGLE_HUD_VISIBILITY = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "toggle_hud_visibility")
            .category(MISC_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getEffect(MobEffects.INVISIBILITY)));
    public static final InputBindingSupplier SHOW_PLAYER_LIST = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "show_player_list")
            .category(MISC_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.PLAYER_HEAD)));
    public static final InputBindingSupplier TAKE_SCREENSHOT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "take_screenshot")
            .category(MISC_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.SPYGLASS)));

    public static final InputBindingSupplier DEBUG_RADIAL = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "debug_radial")
            .category(DEBUG_CATEGORY)
            .allowedContexts(BindContext.IN_GAME, BindContext.RADIAL_MENU));
    public static final InputBindingSupplier TOGGLE_DEBUG_MENU = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "toggle_debug_menu")
            .category(DEBUG_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK)));
    public static final InputBindingSupplier TOGGLE_DEBUG_MENU_FPS = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "toggle_debug_menu_fps")
            .category(DEBUG_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK)));
    //? if >=1.20.3 {
    public static final InputBindingSupplier TOGGLE_DEBUG_MENU_NET = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "toggle_debug_menu_net")
            .category(DEBUG_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK)));
    public static final InputBindingSupplier TOGGLE_DEBUG_MENU_PROF = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "toggle_debug_menu_prof")
            .category(DEBUG_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK)));
    //?} else {
    /*public static final InputBindingSupplier TOGGLE_DEBUG_MENU_CHARTS = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "toggle_debug_menu_charts")
            .category(DEBUG_CATEGORY)
            .allowedContexts(BindContext.IN_GAME)
            .radialCandidate(RadialIcons.getItem(Items.DEBUG_STICK)));
    *///?}

    public static final InputBindingSupplier GUI_PRESS = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_press")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier GUI_BACK = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_back")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.ANY_SCREEN));
    public static final InputBindingSupplier GUI_NEXT_TAB = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_next_tab")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.ANY_SCREEN));
    public static final InputBindingSupplier GUI_PREV_TAB = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_prev_tab")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.ANY_SCREEN));
    public static final InputBindingSupplier GUI_ABSTRACT_ACTION_1 = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_abstract_action_1")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier GUI_ABSTRACT_ACTION_2 = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_abstract_action_2")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier GUI_ABSTRACT_ACTION_3 = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_abstract_action_3")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier GUI_NAVI_UP = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_navi_up")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier GUI_NAVI_DOWN = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_navi_down")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier GUI_NAVI_LEFT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_navi_left")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier GUI_NAVI_RIGHT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "gui_navi_right")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier CYCLE_OPT_FORWARD = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "cycle_opt_forward")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));
    public static final InputBindingSupplier CYCLE_OPT_BACKWARD = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "cycle_opt_backward")
            .category(GUI_CATEGORY)
            .allowedContexts(BindContext.REGULAR_SCREEN));

    public static final InputBindingSupplier RADIAL_MENU = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "radial_menu")
            .category(RADIAL_CATEGORY)
            .allowedContexts(BindContext.IN_GAME, BindContext.RADIAL_MENU));
    public static final InputBindingSupplier RADIAL_AXIS_UP = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "radial_axis_up")
            .category(RADIAL_CATEGORY)
            .allowedContexts(BindContext.RADIAL_MENU));
    public static final InputBindingSupplier RADIAL_AXIS_DOWN = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "radial_axis_down")
            .category(RADIAL_CATEGORY)
            .allowedContexts(BindContext.RADIAL_MENU));
    public static final InputBindingSupplier RADIAL_AXIS_LEFT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "radial_axis_left")
            .category(RADIAL_CATEGORY)
            .allowedContexts(BindContext.RADIAL_MENU));
    public static final InputBindingSupplier RADIAL_AXIS_RIGHT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "radial_axis_right")
            .category(RADIAL_CATEGORY)
            .allowedContexts(BindContext.RADIAL_MENU));

    public static final InputBindingSupplier VMOUSE_MOVE_UP = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_move_up")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_MOVE_DOWN = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_move_down")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_MOVE_LEFT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_move_left")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_MOVE_RIGHT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_move_right")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_SNAP_UP = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_snap_up")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_SNAP_DOWN = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_snap_down")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_SNAP_LEFT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_snap_left")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_SNAP_RIGHT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_snap_right")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_LCLICK = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_lclick")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_RCLICK = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_rclick")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_SHIFT_CLICK = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_shift_click")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_SCROLL_DOWN = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_scroll_down")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_SCROLL_UP = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_scroll_up")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_SHIFT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_shift")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_PAGE_NEXT = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_page_next")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_PAGE_PREV = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_page_prev")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_PAGE_DOWN = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_page_down")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_PAGE_UP = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_page_up")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_COMPAT, BindContext.V_MOUSE_CURSOR));
    public static final InputBindingSupplier VMOUSE_TOGGLE = ControlifyBindApi.get().registerBinding(builder -> builder
            .id("controlify", "vmouse_toggle")
            .category(VMOUSE_CATEGORY)
            .allowedContexts(BindContext.V_MOUSE_CURSOR, BindContext.V_MOUSE_COMPAT, BindContext.ANY_SCREEN));

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

                var identifier = CUtil.rl("fabric-key-binding-api-v1", idPath);

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
