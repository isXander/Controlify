---
title: Built-in Actions
---

# Built-in Actions

These are Controlify's built-in actions, grouped by their category in the controls settings. The name is the one
shown in the controls settings, in English.

The layers column lists the [layers](../developers/actions#built-in-layers) each action receives input in. An
action in more than one layer receives input through whichever of them is reachable.

Use these IDs in [default bindings](../developers/actions#default-bindings), activators, radial menu icons and
keyboard layout [shortcuts](../resource-packs/keyboard-layouts#keys).

## Movement

| ID | Name | Layers |
|---|---|---|
| `controlify:walk_forward` | Walk Forward | `controlify:in_game` |
| `controlify:walk_backward` | Walk Backward | `controlify:in_game` |
| `controlify:strafe_left` | Strafe Left | `controlify:in_game` |
| `controlify:strafe_right` | Strafe Right | `controlify:in_game` |
| `controlify:look_up` | Look Up | `controlify:in_game` |
| `controlify:look_down` | Look Down | `controlify:in_game` |
| `controlify:look_left` | Look Left | `controlify:in_game` |
| `controlify:look_right` | Look Right | `controlify:in_game` |
| `controlify:gyro_button` | Gyro Button | `controlify:in_game` |
| `controlify:jump` | Jump | `controlify:in_game` |
| `controlify:sprint` | Sprint | `controlify:in_game` |
| `controlify:sneak` | Sneak | `controlify:in_game` |

`controlify:gyro_button` only appears in the controls settings for controllers with a gyro.

## Gameplay

| ID | Name | Layers |
|---|---|---|
| `controlify:attack` | Attack | `controlify:in_game` |
| `controlify:use` | Use | `controlify:in_game` |
| `controlify:drop` | Drop Item | `controlify:in_game` |
| `controlify:drop_stack` | Drop Stack | `controlify:in_game` |
| `controlify:pause` | Pause Game | `controlify:in_game` |
| `controlify:change_perspective` | Change Perspective | `controlify:in_game` |
| `controlify:swap_hands` | Swap Hands | `controlify:in_game` |
| `controlify:next_slot` | Next Hotbar Slot | `controlify:in_game` |
| `controlify:prev_slot` | Previous Hotbar Slot | `controlify:in_game` |
| `controlify:hotbar_item_select_radial` | Hotbar Slot Select Radial | `controlify:in_game`<br>`controlify:radial_menu` |
| `controlify:hotbar_slot_1` | Hotbar Slot 1 | `controlify:in_game` |
| `controlify:hotbar_slot_2` | Hotbar Slot 2 | `controlify:in_game` |
| `controlify:hotbar_slot_3` | Hotbar Slot 3 | `controlify:in_game` |
| `controlify:hotbar_slot_4` | Hotbar Slot 4 | `controlify:in_game` |
| `controlify:hotbar_slot_5` | Hotbar Slot 5 | `controlify:in_game` |
| `controlify:hotbar_slot_6` | Hotbar Slot 6 | `controlify:in_game` |
| `controlify:hotbar_slot_7` | Hotbar Slot 7 | `controlify:in_game` |
| `controlify:hotbar_slot_8` | Hotbar Slot 8 | `controlify:in_game` |
| `controlify:hotbar_slot_9` | Hotbar Slot 9 | `controlify:in_game` |
| `controlify:game_mode_switcher` | Game Mode Switcher | `controlify:in_game`<br>`controlify:radial_menu` |

## Inventory

| ID | Name | Layers |
|---|---|---|
| `controlify:inventory` | Open Inventory | `controlify:in_game` |
| `controlify:inv_select` | Select Item | `controlify:container` |
| `controlify:inv_quick_move` | Quick Move | `controlify:container` |
| `controlify:inv_take_half` | Take Half | `controlify:container` |
| `controlify:drop_inventory` | Drop Item (In Containers) | `controlify:container` |

## Creative

| ID | Name | Layers |
|---|---|---|
| `controlify:pick_block` | Pick Block | `controlify:in_game` |
| `controlify:pick_block_nbt` | Pick Block (with NBT) | `controlify:in_game` |
| `controlify:hotbar_load_radial` | Load Creative Hotbar | `controlify:in_game`<br>`controlify:radial_menu` |
| `controlify:hotbar_save_radial` | Save Creative Hotbar | `controlify:in_game`<br>`controlify:radial_menu` |

## Miscellaneous

| ID | Name | Layers |
|---|---|---|
| `controlify:open_chat` | Open Chat | `controlify:in_game` |
| `controlify:toggle_hud_visibility` | Toggle HUD Visibility | `controlify:in_game` |
| `controlify:show_player_list` | Show Player List | `controlify:in_game` |
| `controlify:take_screenshot` | Take Screenshot | `controlify:in_game` |

## Debug

| ID | Name | Layers |
|---|---|---|
| `controlify:debug_radial` | Debug Radial Menu | `controlify:in_game`<br>`controlify:radial_menu` |
| `controlify:toggle_debug_menu` | Toggle Debug Menu (F3) | `controlify:in_game` |
| `controlify:toggle_debug_menu_fps` | Toggle Debug Menu (F3) with FPS | `controlify:in_game` |
| `controlify:toggle_debug_menu_net` | Toggle Debug Menu (F3) with Net Graph | `controlify:in_game` |
| `controlify:toggle_debug_menu_prof` | Toggle Debug Menu (F3) with Profiler | `controlify:in_game` |
| `controlify:debug_reload_chunks` | Reload Chunks | `controlify:in_game` |
| `controlify:debug_toggle_chunk_borders` | Toggle Chunk Boundaries | `controlify:in_game` |
| `controlify:debug_toggle_advanced_tooltips` | Toggle Advanced Tooltips | `controlify:in_game` |
| `controlify:debug_toggle_entity_hitboxes` | Toggle Entity Hitboxes | `controlify:in_game` |
| `controlify:debug_reload_resource_packs` | Reload Resource Packs | `controlify:in_game` |
| `controlify:debug_clear_chat` | Clear Chat | `controlify:in_game` |
| `controlify:debug_start_stop_profiling` | Start/Stop Profiling | `controlify:in_game` |

## GUI

| ID | Name | Layers |
|---|---|---|
| `controlify:gui_press` | GUI Press | `controlify:regular_screen` |
| `controlify:gui_back` | GUI Back | `controlify:screen` |
| `controlify:gui_done` | GUI Done | `controlify:screen` |
| `controlify:gui_next_tab` | GUI Next Tab | `controlify:screen` |
| `controlify:gui_prev_tab` | GUI Previous Tab | `controlify:screen` |
| `controlify:gui_abstract_action_1` | GUI Abstract Action 1 | `controlify:regular_screen` |
| `controlify:gui_abstract_action_2` | GUI Abstract Action 2 | `controlify:regular_screen` |
| `controlify:gui_abstract_action_3` | GUI Abstract Action 3 | `controlify:regular_screen` |
| `controlify:gui_navi_up` | GUI Navi Up | `controlify:regular_screen` |
| `controlify:gui_navi_down` | GUI Navi Down | `controlify:regular_screen` |
| `controlify:gui_navi_left` | GUI Navi Left | `controlify:regular_screen` |
| `controlify:gui_navi_right` | GUI Navi Right | `controlify:regular_screen` |
| `controlify:gui_secondary_navi_up` | GUI Secondary Navi Up | `controlify:container`<br>`controlify:regular_screen` |
| `controlify:gui_secondary_navi_down` | GUI Secondary Navi Down | `controlify:container`<br>`controlify:regular_screen` |
| `controlify:gui_secondary_navi_left` | GUI Secondary Navi Left | `controlify:container`<br>`controlify:regular_screen` |
| `controlify:gui_secondary_navi_right` | GUI Secondary Navi Right | `controlify:container`<br>`controlify:regular_screen` |

The three abstract actions are generic actions that can do different things depending on the current screen.

## Radial Menu

| ID | Name | Layers |
|---|---|---|
| `controlify:radial_menu` | Radial Menu | `controlify:in_game`<br>`controlify:radial_menu` |
| `controlify:radial_axis_up` | Radial Menu Up | `controlify:radial_menu` |
| `controlify:radial_axis_down` | Radial Menu Down | `controlify:radial_menu` |
| `controlify:radial_axis_left` | Radial Menu Left | `controlify:radial_menu` |
| `controlify:radial_axis_right` | Radial Menu Right | `controlify:radial_menu` |

## Virtual Mouse

| ID | Name | Layers |
|---|---|---|
| `controlify:vmouse_move_up` | VMouse Move Up | `controlify:vmouse_cursor` |
| `controlify:vmouse_move_down` | VMouse Move Down | `controlify:vmouse_cursor` |
| `controlify:vmouse_move_left` | VMouse Move Left | `controlify:vmouse_cursor` |
| `controlify:vmouse_move_right` | VMouse Move Right | `controlify:vmouse_cursor` |
| `controlify:vmouse_snap_up` | Snap Mouse Up | `controlify:vmouse_cursor` |
| `controlify:vmouse_snap_down` | Snap Mouse Down | `controlify:vmouse_cursor` |
| `controlify:vmouse_snap_left` | Snap Mouse Left | `controlify:vmouse_cursor` |
| `controlify:vmouse_snap_right` | Snap Mouse Right | `controlify:vmouse_cursor` |
| `controlify:vmouse_lclick` | VMouse LClick | `controlify:vmouse_cursor` |
| `controlify:vmouse_rclick` | VMouse RClick | `controlify:vmouse_cursor` |
| `controlify:vmouse_shift_click` | VMouse Shift Click | `controlify:vmouse_cursor` |
| `controlify:vmouse_scroll_down` | VMouse Scroll Down | `controlify:vmouse_cursor` |
| `controlify:vmouse_scroll_up` | VMouse Scroll Up | `controlify:vmouse_cursor` |
| `controlify:vmouse_shift` | VMouse Key Shift | `controlify:vmouse_cursor` |
| `controlify:vmouse_page_next` | VMouse Page Next | `controlify:vmouse_cursor` |
| `controlify:vmouse_page_prev` | VMouse Page Prev | `controlify:vmouse_cursor` |
| `controlify:vmouse_page_down` | VMouse Page Down | `controlify:vmouse_cursor` |
| `controlify:vmouse_page_up` | VMouse Page Up | `controlify:vmouse_cursor` |
| `controlify:vmouse_toggle` | Toggle Virtual Mouse | `controlify:screen` |

## Actions for other mods' key mappings

Controlify also creates an action for each key mapping another mod adds, unless that mod
[handles the key mapping itself](../developers/actions#key-mappings). Its ID is `controlify_modded:<name>`, where
`<name>` is the key mapping's name in lower case, with any character other than `a-z`, `0-9`, `/`, `.`, `_` and
`-` replaced by `_`. These actions are in the `controlify:in_game` layer.
