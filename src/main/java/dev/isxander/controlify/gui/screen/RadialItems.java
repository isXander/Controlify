/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.bindings.RadialIconManager;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class RadialItems {
	public static final RadialMenuScreen.RadialItem EMPTY_ACTION = new RadialItemRecord(Component.empty(), RadialIcon.EMPTY, () -> false, RadialIconManager.EMPTY);

	public static RadialMenuScreen.RadialItem[] createBindings(ControllerEntity controller) {
		List<Identifier> actions = controller.settings().input.radialMenu.radialActions;
		RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[actions.size()];

		for (int i = 0; i < actions.size(); i++) {
			items[i] = getItemForBinding(actions.get(i), controller);
		}

		return items;
	}

	public static List<RadialMenuScreen.RadialItem[]> createBindingPages(ControllerEntity controller) {
		RadialMenuScreen.RadialItem[] items = createBindings(controller);
		return RadialMenuPages.partition(Arrays.asList(items)).stream()
				.map(page -> page.toArray(RadialMenuScreen.RadialItem[]::new))
				.toList();
	}

	public static RadialPage createGameModes() {
		RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[4];

		items[0] = new GameModeItem(GameType.CREATIVE);
		items[1] = new GameModeItem(GameType.SURVIVAL);
		items[2] = new GameModeItem(GameType.ADVENTURE);
		items[3] = new GameModeItem(GameType.SPECTATOR);

		return new RadialPage(
			Component.translatable("controlify.binding.controlify.game_mode_switcher"),
			items
		);
	}

	public static RadialPage createHotbarSave() {
		Minecraft mc = Minecraft.getInstance();
		RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[9];

		for (int i = 0; i < 9; i++) {
			int j = i;
			items[i] = new RadialItemRecord(
					Component.translatable("controlify.radial.hotbar", Component.literal(Integer.toString(j + 1))),
					getIconForHotbar(i, true),
					() -> {
						CreativeModeInventoryScreen.handleHotbarLoadOrSave(mc, j, false, true);
						return true;
					},
					CUtil.rl("hotbar_save/" + j)
			);
		}

		return new RadialPage(
			Component.translatable("controlify.radial.hotbar_save_hint"),
			items
		);
	}

	public static RadialPage createHotbarLoad() {
		Minecraft mc = Minecraft.getInstance();
		RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[9];

		for (int i = 0; i < items.length; i++) {
			int j = i;
			items[i] = new RadialItemRecord(
					Component.translatable("controlify.radial.hotbar", Component.literal(Integer.toString(j + 1))),
					getIconForHotbar(i, true),
					() -> {
						CreativeModeInventoryScreen.handleHotbarLoadOrSave(mc, j, true, false);
						return true;
					},
					CUtil.rl("hotbar_load/" + j)
			);
		}

		return new RadialPage(
			Component.translatable("controlify.radial.hotbar_load_hint"),
			items
		);
	}

	public static RadialMenuScreen.RadialItem[] createHotbarItemSelect() {
		Minecraft mc = Minecraft.getInstance();
		RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[9];

		for (int i = 0; i < items.length; i++) {
			int j = i;
			items[i] = new RadialItemRecord(
					Component.translatable("controlify.radial.hotbar", Component.literal(Integer.toString(j + 1))),
					getIconForItem(mc.player.getInventory().getItem(j)),
					() -> {
						mc.player.getInventory().setSelectedSlot(j);
						return true;
					},
					CUtil.rl("hotbar_item_select/" + j)
			);
		}

		return items;
	}

	public static RadialMenuScreen.RadialItem[] createDebug(ControllerEntity controller) {
		return List.of(
				ControlifyBindings.TOGGLE_DEBUG_MENU,
				ControlifyBindings.TOGGLE_DEBUG_MENU_FPS,
				ControlifyBindings.TOGGLE_DEBUG_MENU_NET,
				ControlifyBindings.TOGGLE_DEBUG_MENU_PROF,
				ControlifyBindings.DEBUG_RELOAD_CHUNKS,
				ControlifyBindings.DEBUG_TOGGLE_CHUNK_BORDERS,
				ControlifyBindings.DEBUG_TOGGLE_ADVANCED_TOOLTIPS,
				ControlifyBindings.DEBUG_TOGGLE_ENTITY_HITBOXES,
				ControlifyBindings.DEBUG_RELOAD_RESOURCE_PACKS,
				ControlifyBindings.DEBUG_CLEAR_CHAT,
				ControlifyBindings.DEBUG_START_STOP_PROFILING
		).stream()
				.map(InputBindingSupplier::bindId)
				.map(id -> getItemForBinding(id, controller))
				.toArray(RadialMenuScreen.RadialItem[]::new);
	}

	private static RadialIcon getIconForHotbar(int hotbarIndex, boolean showNumbers) {
		Minecraft mc = Minecraft.getInstance();
		Hotbar hotbar = mc.getHotbarManager().get(hotbarIndex);

		List<ItemStack> hotbarItems = hotbar.load(mc.player.registryAccess());

		for (int i = 0; i < 9; i++) {
			ItemStack stack = hotbarItems.get(i);

			if (!stack.is(Items.AIR)) {
				RadialIcon icon = getIconForItem(stack);
				return showNumbers
						? icon.withOverlay(Component.literal(Integer.toString(hotbarIndex + 1)))
						: icon;
			}
		}

		return showNumbers
				? RadialIcon.EMPTY.withOverlay(Component.literal(Integer.toString(hotbarIndex + 1)))
				: RadialIcon.EMPTY;
	}

	private static RadialIcon getIconForItem(ItemStack stack) {
		Identifier model = stack.get(DataComponents.ITEM_MODEL);
		return model != null ? RadialIcon.model(model) : RadialIcon.EMPTY;
	}

	private static RadialMenuScreen.RadialItem getItemForBinding(Identifier id, ControllerEntity controller) {
		InputBinding binding = controller.input().orElseThrow().getBinding(id);

		if (binding == null || !RadialIconManager.INSTANCE.isRadialCandidate(binding)) {
			CUtil.LOGGER.warn("Binding {} does not exist or is not a radial candidate", binding);
			return EMPTY_ACTION;
		}

		return new RadialItemRecord(
				binding.name(),
				RadialIconManager.INSTANCE.getIcon(binding),
				() -> {
					binding.fakePress();
					return true;
				},
				id
		);
	}

	public record RadialPage(Component name, RadialMenuScreen.RadialItem[] items) {
	}

	private record RadialItemRecord(Component name, RadialIcon icon, Supplier<Boolean> action, Identifier id) implements RadialMenuScreen.RadialItem {
		@Override
		public boolean playAction() {
			return action.get();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj instanceof RadialItemRecord record) {
				return id.equals(record.id);
			}
			return false;
		}
	}

	private static class GameModeItem implements RadialMenuScreen.RadialItem {
		private final GameType gameType;
		private final Component name;
		private final RadialIcon icon;
		private final String command;

		public GameModeItem(GameType gameType) {
			this.gameType = gameType;
			this.name = gameType.getShortDisplayName();
			Identifier iconId = switch (gameType) {
				case CREATIVE -> BuiltInRegistries.ITEM.getKey(Items.GRASS_BLOCK);
				case SURVIVAL -> BuiltInRegistries.ITEM.getKey(Items.IRON_SWORD);
				case ADVENTURE -> BuiltInRegistries.ITEM.getKey(Items.MAP);
				case SPECTATOR -> BuiltInRegistries.ITEM.getKey(Items.ENDER_EYE);
			};
			this.icon = RadialIcon.model(iconId);
			this.command = switch (gameType) {
				case CREATIVE -> "gamemode creative";
				case SURVIVAL -> "gamemode survival";
				case ADVENTURE -> "gamemode adventure";
				case SPECTATOR -> "gamemode spectator";
			};
		}

		@Override
		public Component name() {
			return name;
		}

		@Override
		public RadialIcon icon() {
			return icon;
		}

		@Override
		public boolean playAction() {
			Minecraft client = Minecraft.getInstance();
			if (client.gameMode != null && client.player != null) {
				if (client.canSwitchGameMode()
					&& GameModeCommand.PERMISSION_CHECK.check(client.player.permissions())
					&& client.gameMode.getPlayerMode() != gameType) {
					client.player.connection.sendCommand(command);
					return true;
				}
			}

			return false;
		}
	}

}
