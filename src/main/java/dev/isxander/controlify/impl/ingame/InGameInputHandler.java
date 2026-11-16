/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.ingame.look.CameraLookSink;
import dev.isxander.controlify.impl.ingame.look.LookPipeline;
import dev.isxander.controlify.impl.ingame.look.LookRegistryImpl;
import dev.isxander.controlify.impl.gui.screen.RadialItems.RadialPage;
import dev.isxander.controlify.impl.gui.screen.RadialItems;
import dev.isxander.controlify.impl.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.impl.server.ServerPolicies;
import dev.isxander.controlify.impl.utils.DebugOverlayHelper;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.mixins.feature.steamdeck.ScreenshotAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.commands.GameModeCommand;
//? if <26.3
//import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InGameInputHandler {
	private final ControllerImpl controller;
	private final Controlify controlify;
	private final Minecraft minecraft;
	private final LookPipeline lookPipeline;

	private boolean lookWasPossible;

	private boolean shouldShowPlayerList;

	public InGameInputHandler(ControllerImpl controller) {
		this.controller = controller;
		this.minecraft = Minecraft.getInstance();
		this.controlify = Controlify.instance();
		this.lookPipeline = new LookPipeline(controller, LookRegistryImpl.INSTANCE, new CameraLookSink(this.minecraft));
	}

	public void fixedUpdate() {
		boolean isController = ControllerPlayerMovement.shouldBeControllerInput();

		ControllerPlayerMovement.ensureCorrectInput(minecraft.player);

		if (isController) {
			handleKeybinds();
			preventFlyDrifting();
		}
	}

	protected void handleKeybinds() {
		if (MinecraftUtil.getScreen() != null) {
			return;
		}

		if (ControlifyActions.PAUSE.on(controller).pulse().consumeAllPulses()) {
			minecraft.pauseGame(false);
		}
		if (minecraft.player != null) {
			Inventory inventory = minecraft.player.getInventory();

			while (ControlifyActions.NEXT_SLOT.on(controller).pulse().consumePulse()) {
				inventory.setSelectedSlot((inventory.getSelectedSlot() + 1) % Inventory.getSelectionSize());
			}
			while (ControlifyActions.PREV_SLOT.on(controller).pulse().consumePulse()) {
				inventory.setSelectedSlot((inventory.getSelectedSlot() - 1 + Inventory.getSelectionSize()) % Inventory.getSelectionSize());
			}

			var hotbarSlots = List.of(
					ControlifyActions.HOTBAR_SLOT_1,
					ControlifyActions.HOTBAR_SLOT_2,
					ControlifyActions.HOTBAR_SLOT_3,
					ControlifyActions.HOTBAR_SLOT_4,
					ControlifyActions.HOTBAR_SLOT_5,
					ControlifyActions.HOTBAR_SLOT_6,
					ControlifyActions.HOTBAR_SLOT_7,
					ControlifyActions.HOTBAR_SLOT_8,
					ControlifyActions.HOTBAR_SLOT_9
			);
			for (int i = 0; i < hotbarSlots.size(); i++) {
				if (hotbarSlots.get(i).on(controller).pulse().consumeAllPulses()) {
					inventory.setSelectedSlot(i);
				}
			}

			if (!minecraft.player.isSpectator()) {
				if (ControlifyActions.DROP_STACK.on(controller).pulse().consumeAllPulses()) {
					dropItem(minecraft.player, true);
					ControlifyActions.DROP_INGAME.on(controller).pulse().consumeAllPulses();
				} else {
					while (ControlifyActions.DROP_INGAME.on(controller).pulse().consumePulse()) {
						dropItem(minecraft.player, false);
					}
				}

				while (ControlifyActions.SWAP_HANDS.on(controller).pulse().consumePulse()) {
					minecraft.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
				}
			}

			if (ControlifyActions.INVENTORY.on(controller).pulse().consumeAllPulses()) {
				if (minecraft.gameMode.isServerControlledInventory()) {
					minecraft.player.sendOpenInventory();
				} else {
					minecraft.getTutorial().onOpenInventory();
					MinecraftUtil.setScreen(new InventoryScreen(minecraft.player));
				}
			}

			while (ControlifyActions.CHANGE_PERSPECTIVE.on(controller).pulse().consumePulse()) {
				CameraType cameraType = minecraft.options.getCameraType();
				minecraft.options.setCameraType(minecraft.options.getCameraType().cycle());
				if (cameraType.isFirstPerson() != minecraft.options.getCameraType().isFirstPerson()) {
					minecraft.gameRenderer.checkEntityPostEffect(minecraft.options.getCameraType().isFirstPerson() ? minecraft.getCameraEntity() : null);
				}

				//? if <26.2
				//minecraft.levelRenderer.needsUpdate();
			}
		}
		while (ControlifyActions.TOGGLE_HUD_VISIBILITY.on(controller).pulse().consumePulse()) {
			//? if >=26.2 {
			minecraft.gui.hud.toggle();
			//?} else {
			//minecraft.options.hideGui = !minecraft.options.hideGui;
			//?}
		}

		while (ControlifyActions.SHOW_PLAYER_LIST.on(controller).pulse().consumePulse()) {
			shouldShowPlayerList = !shouldShowPlayerList;
		}

		if (ControlifyActions.OPEN_CHAT.on(controller).pulse().consumeAllPulses()) {
			//? if >=26.2 {
			minecraft.gui.openChatScreen(ChatComponent.ChatMethod.MESSAGE);
			//?} else {
			//minecraft.openChatScreen(ChatComponent.ChatMethod.MESSAGE);
			//?}
		}

		while (ControlifyActions.TOGGLE_DEBUG_MENU.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleOverlay();
		}
		while (ControlifyActions.TOGGLE_DEBUG_MENU_FPS.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleFpsOverlay();
		}
		while (ControlifyActions.TOGGLE_DEBUG_MENU_NET.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleNetworkOverlay();
		}
		while (ControlifyActions.TOGGLE_DEBUG_MENU_PROF.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleProfilerOverlay();
		}
		if (ControlifyActions.DEBUG_RELOAD_CHUNKS.on(controller).pulse().consumeAllPulses()) {
			DebugOverlayHelper.reloadChunks();
		}
		while (ControlifyActions.DEBUG_TOGGLE_CHUNK_BORDERS.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleChunkBorders();
		}
		while (ControlifyActions.DEBUG_TOGGLE_ADVANCED_TOOLTIPS.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleAdvancedTooltips();
		}
		while (ControlifyActions.DEBUG_TOGGLE_ENTITY_HITBOXES.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleEntityHitboxes();
		}
		if (ControlifyActions.DEBUG_RELOAD_RESOURCE_PACKS.on(controller).pulse().consumeAllPulses()) {
			DebugOverlayHelper.reloadResourcePacks();
		}
		if (ControlifyActions.DEBUG_CLEAR_CHAT.on(controller).pulse().consumeAllPulses()) {
			DebugOverlayHelper.clearChat();
		}
		while (ControlifyActions.DEBUG_START_STOP_PROFILING.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.startStopProfiling();
		}
		if (ControlifyActions.DEBUG_RADIAL.on(controller).pulse().consumeAllPulses()) {
			MinecraftUtil.setScreen(new RadialMenuScreen(
					controller,
					ControlifyActions.DEBUG_RADIAL,
					RadialItems.createDebug(controller),
					Component.empty(),
					null
			));
		}

		if (ControlifyActions.TAKE_SCREENSHOT.on(controller).pulse().consumeAllPulses()) {
			// get file before it takes and writes the screenshot (which changes the next name)
			File screenshotFile = ScreenshotAccessor.controlify$invokeGetFile(
					new File(minecraft.gameDirectory, "screenshots")
			);

			Screenshot.grab(
					this.minecraft.gameDirectory,
					//? if >=26.2 {
					this.minecraft.gameRenderer.mainRenderTarget(),
					//?} else {
					//this.minecraft.getMainRenderTarget(),
					//?}
					component -> this.minecraft.execute(() -> {
						//? if >=26.2 {
						this.minecraft.gui.hud.getChat()
						//?} else {
						//this.minecraft.gui.getChat()
						//?}
								.addClientSystemMessage(component);
					})
			);
		}

		if (ControlifyActions.PICK_BLOCK.on(controller).pulse().consumeAllPulses()) {
			((PickBlockAccessor) minecraft).controlify$pickBlock();
		}
		if (ControlifyActions.PICK_BLOCK_NBT.on(controller).pulse().consumeAllPulses()) {
			((PickBlockAccessor) minecraft).controlify$pickBlockWithNbt();
		}

		if (ControlifyActions.RADIAL_MENU.on(controller).pulse().consumeAllPulses()) {
			var bonusPages = new ArrayList<RadialPage>();
			if (canSwitchGameMode()) {
				bonusPages.add(RadialItems.createGameModes());
			}

			MinecraftUtil.setScreen(new RadialMenuScreen(
					controller,
					ControlifyActions.RADIAL_MENU,
					RadialItems.createActionPages(controller),
					bonusPages,
					Component.translatable("controlify.radial_menu.configure_hint"),
					null
			));
		}

		if (ControlifyActions.GAME_MODE_SWITCHER.on(controller).pulse().consumeAllPulses() && canSwitchGameMode()) {
			MinecraftUtil.setScreen(new RadialMenuScreen(
					controller,
					ControlifyActions.GAME_MODE_SWITCHER,
					RadialItems.createGameModes().items(),
					Component.empty(),
					null)
			);
		}

		if (ControlifyActions.HOTBAR_SLOT_SELECT.on(controller).pulse().consumeAllPulses()) {
			MinecraftUtil.setScreen(new RadialMenuScreen(
					controller,
					ControlifyActions.HOTBAR_SLOT_SELECT,
					RadialItems.createHotbarItemSelect(),
					Component.empty(),
					null
			));
		}

		if (minecraft.player != null && minecraft.player.hasInfiniteMaterials()) {
			if (ControlifyActions.HOTBAR_LOAD_RADIAL.on(controller).pulse().consumeAllPulses()) {
				MinecraftUtil.setScreen(new RadialMenuScreen(
						controller,
						ControlifyActions.HOTBAR_LOAD_RADIAL,
						RadialItems.createHotbarLoad().items(),
						Component.translatable("controlify.radial.hotbar_load_hint"),
						null
				));
			}
			if (ControlifyActions.HOTBAR_SAVE_RADIAL.on(controller).pulse().consumeAllPulses()) {
				MinecraftUtil.setScreen(new RadialMenuScreen(
						controller,
						ControlifyActions.HOTBAR_SAVE_RADIAL,
						RadialItems.createHotbarSave().items(),
						Component.translatable("controlify.radial.hotbar_save_hint"),
						null
				));
			}
		}
	}

	private boolean canSwitchGameMode() {
		return minecraft.canSwitchGameMode()
				&& minecraft.player != null
				&& GameModeCommand.PERMISSION_CHECK.check(minecraft.player.permissions());
	}

	public void processPlayerLook(float deltaTime) {
		LocalPlayer player = this.minecraft.player;

		if (!ControllerPlayerMovement.shouldBeControllerInput() || !canProcessLookInput()) {
			if (this.lookWasPossible) {
				this.lookPipeline.reset();
				this.lookWasPossible = false;
			}
			return;
		}
		this.lookWasPossible = true;

		this.lookPipeline.process(player, deltaTime);
	}

	public boolean shouldShowPlayerList() {
		return this.shouldShowPlayerList;
	}

	public void preventFlyDrifting() {
		if (!controller.settings().generic.disableFlyDrifting || ServerPolicies.DISABLE_FLY_DRIFTING.get()) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player != null && player.getAbilities().flying && !player.onGround()) {
			Vec3 motion = player.getDeltaMovement();
			double x = motion.x;
			double y = motion.y;
			double z = motion.z;

			boolean jumping = player.input.keyPresses.jump();
			boolean shiftKeyDown = player.input.keyPresses.shift();

			if (!jumping) {
				y = Math.min(y, 0);
			}
			if (!shiftKeyDown) {
				y = Math.max(y, 0);
			}

			Vec2 moveVec = getMoveVec(player.input);
			if (moveVec.x == 0 && moveVec.y == 0) {
				x = 0;
				z = 0;
			}

			player.setDeltaMovement(x, y, z);
		}
	}

	private boolean canProcessLookInput() {
		boolean mouseNotGrabbed = !minecraft.mouseHandler.isMouseGrabbed() && !controlify.config().getSettings().globalSettings().outOfFocusInput;
		boolean outOfFocus = !minecraft.isWindowActive() && !controlify.config().getSettings().globalSettings().outOfFocusInput;
		boolean screenVisible = MinecraftUtil.getScreen() != null;
		boolean playerExists = minecraft.player != null;

		return !mouseNotGrabbed && !outOfFocus && !screenVisible && playerExists;
	}

	public static Vec2 getMoveVec(ClientInput input) {
		return input.getMoveVector();
	}

	private static boolean dropItem(LocalPlayer player, boolean all) {
		//? if >=26.3 {
		Objects.requireNonNull(Minecraft.getInstance().gameMode).dropItem(player, all);
		return !player.getInventory().getSelectedItem().isEmpty();
		//?} else {
		/*if (player.drop(all)) {
			player.swing(InteractionHand.MAIN_HAND);
			return true;
		}
		return false;
		*///?}
	}
}
