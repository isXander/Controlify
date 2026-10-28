/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.action.ActionEvents;
import dev.isxander.controlify.api.action.ActionProxyManager;
import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.ActionSpecBuilder;
import dev.isxander.controlify.api.action.BuiltinActionLayers;
import dev.isxander.controlify.impl.action.proxy.ActionProxyManagerImpl;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.platform.client.PlatformClientUtil;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public final class ControlifyActions {
	public static final String MODDED_ACTION_NAMESPACE = "controlify_modded";

	private static final Options options = Minecraft.getInstance().options;

	public static final Component MOVEMENT_CATEGORY = KeyMapping.Category.MOVEMENT.label();
	public static final Component GAMEPLAY_CATEGORY = KeyMapping.Category.GAMEPLAY.label();
	public static final Component INVENTORY_CATEGORY = KeyMapping.Category.INVENTORY.label();
	public static final Component CREATIVE_CATEGORY = KeyMapping.Category.CREATIVE.label();
	public static final Component MISC_CATEGORY = KeyMapping.Category.MISC.label();
	public static final Component DEBUG_CATEGORY = Component.translatable("controlify.binding_category.debug");
	public static final Component GUI_CATEGORY = Component.translatable("controlify.binding_category.gui");
	public static final Component RADIAL_CATEGORY = Component.translatable("controlify.gui.radial_menu");
	public static final Component VMOUSE_CATEGORY = Component.translatable("controlify.binding_category.vmouse");

	public static final ActionAccessor WALK_FORWARD = register(ActionSpec.builder()
			.id(CUtil.rl("walk_forward"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor WALK_BACKWARD = register(ActionSpec.builder()
			.id(CUtil.rl("walk_backward"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor WALK_LEFT = register(ActionSpec.builder()
			.id(CUtil.rl("strafe_left"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor WALK_RIGHT = register(ActionSpec.builder()
			.id(CUtil.rl("strafe_right"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor LOOK_UP = register(ActionSpec.builder()
			.id(CUtil.rl("look_up"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor LOOK_DOWN = register(ActionSpec.builder()
			.id(CUtil.rl("look_down"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor LOOK_LEFT = register(ActionSpec.builder()
			.id(CUtil.rl("look_left"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor LOOK_RIGHT = register(ActionSpec.builder()
			.id(CUtil.rl("look_right"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor GYRO_BUTTON = register(ActionSpec.builder()
			.id(CUtil.rl("gyro_button"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME)
			.shouldExpose(c -> c.gyro().isPresent()));
	public static final ActionAccessor JUMP = register(ActionSpec.builder()
			.id(CUtil.rl("jump"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor SPRINT = register(ActionSpec.builder()
			.id(CUtil.rl("sprint"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor SNEAK = register(ActionSpec.builder()
			.id(CUtil.rl("sneak"))
			.category(MOVEMENT_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor ATTACK = register(ActionSpec.builder()
			.id(CUtil.rl("attack"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor USE = register(ActionSpec.builder()
			.id(CUtil.rl("use"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DROP_INGAME = register(ActionSpec.builder()
			.id(CUtil.rl("drop"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DROP_STACK = register(ActionSpec.builder()
			.id(CUtil.rl("drop_stack"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor PAUSE = register(ActionSpec.builder()
			.id(CUtil.rl("pause"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME, BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor CHANGE_PERSPECTIVE = register(ActionSpec.builder()
			.id(CUtil.rl("change_perspective"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor SWAP_HANDS = register(ActionSpec.builder()
			.id(CUtil.rl("swap_hands"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor NEXT_SLOT = register(ActionSpec.builder()
			.id(CUtil.rl("next_slot"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor PREV_SLOT = register(ActionSpec.builder()
			.id(CUtil.rl("prev_slot"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_SELECT = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_item_select_radial"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME, BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor HOTBAR_SLOT_1 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_1"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_2 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_2"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_3 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_3"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_4 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_4"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_5 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_5"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_6 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_6"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_7 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_7"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_8 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_8"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_SLOT_9 = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_slot_9"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor GAME_MODE_SWITCHER = register(ActionSpec.builder()
			.id(CUtil.rl("game_mode_switcher"))
			.category(GAMEPLAY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME, BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor INVENTORY = register(ActionSpec.builder()
			.id(CUtil.rl("inventory"))
			.category(INVENTORY_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor INV_SELECT = register(ActionSpec.builder()
			.id(CUtil.rl("inv_select"))
			.category(INVENTORY_CATEGORY)
			.layer(BuiltinActionLayers.CONTAINER));
	public static final ActionAccessor INV_QUICK_MOVE = register(ActionSpec.builder()
			.id(CUtil.rl("inv_quick_move"))
			.category(INVENTORY_CATEGORY)
			.layer(BuiltinActionLayers.CONTAINER));
	public static final ActionAccessor INV_TAKE_HALF = register(ActionSpec.builder()
			.id(CUtil.rl("inv_take_half"))
			.category(INVENTORY_CATEGORY)
			.layer(BuiltinActionLayers.CONTAINER));
	public static final ActionAccessor DROP_INVENTORY = register(ActionSpec.builder()
			.id(CUtil.rl("drop_inventory"))
			.category(INVENTORY_CATEGORY)
			.layer(BuiltinActionLayers.CONTAINER, BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor PICK_BLOCK = register(ActionSpec.builder()
			.id(CUtil.rl("pick_block"))
			.category(CREATIVE_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor PICK_BLOCK_NBT = register(ActionSpec.builder()
			.id(CUtil.rl("pick_block_nbt"))
			.category(CREATIVE_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor HOTBAR_LOAD_RADIAL = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_load_radial"))
			.category(CREATIVE_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME, BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor HOTBAR_SAVE_RADIAL = register(ActionSpec.builder()
			.id(CUtil.rl("hotbar_save_radial"))
			.category(CREATIVE_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME, BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor OPEN_CHAT = register(ActionSpec.builder()
			.id(CUtil.rl("open_chat"))
			.category(MISC_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor TOGGLE_HUD_VISIBILITY = register(ActionSpec.builder()
			.id(CUtil.rl("toggle_hud_visibility"))
			.category(MISC_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor SHOW_PLAYER_LIST = register(ActionSpec.builder()
			.id(CUtil.rl("show_player_list"))
			.category(MISC_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor TAKE_SCREENSHOT = register(ActionSpec.builder()
			.id(CUtil.rl("take_screenshot"))
			.category(MISC_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DEBUG_RADIAL = register(ActionSpec.builder()
			.id(CUtil.rl("debug_radial"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME, BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor TOGGLE_DEBUG_MENU = register(ActionSpec.builder()
			.id(CUtil.rl("toggle_debug_menu"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor TOGGLE_DEBUG_MENU_FPS = register(ActionSpec.builder()
			.id(CUtil.rl("toggle_debug_menu_fps"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor TOGGLE_DEBUG_MENU_NET = register(ActionSpec.builder()
			.id(CUtil.rl("toggle_debug_menu_net"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor TOGGLE_DEBUG_MENU_PROF = register(ActionSpec.builder()
			.id(CUtil.rl("toggle_debug_menu_prof"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DEBUG_RELOAD_CHUNKS = register(ActionSpec.builder()
			.id(CUtil.rl("debug_reload_chunks"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DEBUG_TOGGLE_CHUNK_BORDERS = register(ActionSpec.builder()
			.id(CUtil.rl("debug_toggle_chunk_borders"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DEBUG_TOGGLE_ADVANCED_TOOLTIPS = register(ActionSpec.builder()
			.id(CUtil.rl("debug_toggle_advanced_tooltips"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DEBUG_TOGGLE_ENTITY_HITBOXES = register(ActionSpec.builder()
			.id(CUtil.rl("debug_toggle_entity_hitboxes"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DEBUG_RELOAD_RESOURCE_PACKS = register(ActionSpec.builder()
			.id(CUtil.rl("debug_reload_resource_packs"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DEBUG_CLEAR_CHAT = register(ActionSpec.builder()
			.id(CUtil.rl("debug_clear_chat"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor DEBUG_START_STOP_PROFILING = register(ActionSpec.builder()
			.id(CUtil.rl("debug_start_stop_profiling"))
			.category(DEBUG_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME));
	public static final ActionAccessor GUI_PRESS = register(ActionSpec.builder()
			.id(CUtil.rl("gui_press"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_BACK = register(ActionSpec.builder()
			.id(CUtil.rl("gui_back"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.ANY_SCREEN));
	public static final ActionAccessor GUI_NEXT_TAB = register(ActionSpec.builder()
			.id(CUtil.rl("gui_next_tab"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.ANY_SCREEN));
	public static final ActionAccessor GUI_PREV_TAB = register(ActionSpec.builder()
			.id(CUtil.rl("gui_prev_tab"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.ANY_SCREEN));
	public static final ActionAccessor GUI_ABSTRACT_ACTION_1 = register(ActionSpec.builder()
			.id(CUtil.rl("gui_abstract_action_1"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_ABSTRACT_ACTION_2 = register(ActionSpec.builder()
			.id(CUtil.rl("gui_abstract_action_2"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_ABSTRACT_ACTION_3 = register(ActionSpec.builder()
			.id(CUtil.rl("gui_abstract_action_3"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_NAVI_UP = register(ActionSpec.builder()
			.id(CUtil.rl("gui_navi_up"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_NAVI_DOWN = register(ActionSpec.builder()
			.id(CUtil.rl("gui_navi_down"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_NAVI_LEFT = register(ActionSpec.builder()
			.id(CUtil.rl("gui_navi_left"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_NAVI_RIGHT = register(ActionSpec.builder()
			.id(CUtil.rl("gui_navi_right"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_SECONDARY_NAVI_UP = register(ActionSpec.builder()
			.id(CUtil.rl("gui_secondary_navi_up"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.CONTAINER, BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_SECONDARY_NAVI_DOWN = register(ActionSpec.builder()
			.id(CUtil.rl("gui_secondary_navi_down"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.CONTAINER, BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_SECONDARY_NAVI_LEFT = register(ActionSpec.builder()
			.id(CUtil.rl("gui_secondary_navi_left"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.CONTAINER, BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor GUI_SECONDARY_NAVI_RIGHT = register(ActionSpec.builder()
			.id(CUtil.rl("gui_secondary_navi_right"))
			.category(GUI_CATEGORY)
			.layer(BuiltinActionLayers.CONTAINER, BuiltinActionLayers.REGULAR_SCREEN));
	public static final ActionAccessor RADIAL_MENU = register(ActionSpec.builder()
			.id(CUtil.rl("radial_menu"))
			.category(RADIAL_CATEGORY)
			.layer(BuiltinActionLayers.IN_GAME, BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor RADIAL_AXIS_UP = register(ActionSpec.builder()
			.id(CUtil.rl("radial_axis_up"))
			.category(RADIAL_CATEGORY)
			.layer(BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor RADIAL_AXIS_DOWN = register(ActionSpec.builder()
			.id(CUtil.rl("radial_axis_down"))
			.category(RADIAL_CATEGORY)
			.layer(BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor RADIAL_AXIS_LEFT = register(ActionSpec.builder()
			.id(CUtil.rl("radial_axis_left"))
			.category(RADIAL_CATEGORY)
			.layer(BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor RADIAL_AXIS_RIGHT = register(ActionSpec.builder()
			.id(CUtil.rl("radial_axis_right"))
			.category(RADIAL_CATEGORY)
			.layer(BuiltinActionLayers.RADIAL_MENU));
	public static final ActionAccessor VMOUSE_MOVE_UP = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_move_up"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_MOVE_DOWN = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_move_down"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_MOVE_LEFT = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_move_left"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_MOVE_RIGHT = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_move_right"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_SNAP_UP = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_snap_up"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_SNAP_DOWN = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_snap_down"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_SNAP_LEFT = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_snap_left"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_SNAP_RIGHT = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_snap_right"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_LCLICK = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_lclick"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_RCLICK = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_rclick"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_SHIFT_CLICK = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_shift_click"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_SCROLL_DOWN = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_scroll_down"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_SCROLL_UP = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_scroll_up"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_SHIFT = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_shift"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_PAGE_NEXT = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_page_next"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_PAGE_PREV = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_page_prev"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_PAGE_DOWN = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_page_down"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_PAGE_UP = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_page_up"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.V_MOUSE_CURSOR));
	public static final ActionAccessor VMOUSE_TOGGLE = register(ActionSpec.builder()
			.id(CUtil.rl("vmouse_toggle"))
			.category(VMOUSE_CATEGORY)
			.layer(BuiltinActionLayers.V_MOUSE_CURSOR, BuiltinActionLayers.V_MOUSE_COMPAT, BuiltinActionLayers.ANY_SCREEN));

	private static ActionAccessor register(ActionSpecBuilder builder) {
		return ActionSpecRegistryImpl.INSTANCE.register(builder.build());
	}

	public static void init() {
		ActionEvents.REGISTER_PROXIES.subscribeForever(ControlifyActions::registerProxies);
	}

	private static void registerProxies(ActionProxyManager proxies) {
		proxies.proxyKeyMapping(SPRINT, options.keySprint,
				controller -> ((ControllerImpl) controller).settings().generic.toggleSprint);
		proxies.proxyKeyMapping(ATTACK, options.keyAttack);
		proxies.proxyKeyMapping(USE, options.keyUse);
	}

	public static void registerModdedActions() {
		for (KeyMapping keyMapping : PlatformClientUtil.getModdedKeyMappings()) {
			if (!ActionProxyManagerImpl.INSTANCE.shouldAutomaticallyProxyKeyMapping(keyMapping)) {
				continue;
			}
			try {
				String idPath = keyMapping.getName().toLowerCase(Locale.ROOT)
						.replaceAll("[^a-z0-9/._-]", "_").trim();
				Identifier id = Identifier.fromNamespaceAndPath(MODDED_ACTION_NAMESPACE, idPath);
				ActionAccessor action = register(ActionSpec.builder()
						.id(id)
						.name(Component.translatable(keyMapping.getName()))
						.description(Component.translatable("controlify.custom_binding.vanilla_description").withStyle(ChatFormatting.GRAY))
						.category(keyMapping.getCategory().label())
						.layer(BuiltinActionLayers.IN_GAME));
				ActionProxyManagerImpl.INSTANCE.proxyKeyMapping(action, keyMapping);
			} catch (Exception e) {
				CUtil.LOGGER.error("Failed to automatically register modded action: {}", keyMapping.getName(), e);
			}
		}
	}

	private ControlifyActions() {
	}
}
