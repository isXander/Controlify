/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame;

import dev.isxander.controlify.api.ingame.LookInputModifier;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.config.settings.profile.GyroSettings;
import dev.isxander.controlify.impl.config.settings.profile.InputSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.impl.controller.gyro.GyroComponentImpl;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import dev.isxander.controlify.impl.driver.steamdeck.SteamDeckDriver;
import dev.isxander.controlify.impl.gui.screen.RadialItems.RadialPage;
import dev.isxander.controlify.impl.gui.screen.RadialItems;
import dev.isxander.controlify.impl.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.impl.server.ServerPolicies;
import dev.isxander.controlify.impl.utils.ControllerUtils;
import dev.isxander.controlify.impl.utils.DebugOverlayHelper;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.impl.utils.animation.api.Animation;
import dev.isxander.controlify.impl.utils.animation.api.EasingFunction;
import dev.isxander.controlify.impl.utils.event.EventBus;
import dev.isxander.controlify.mixins.feature.steamdeck.ScreenshotAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InGameInputHandler {
	public static final class Events {
		public static final EventBus<LookInputModifier> LOOK_INPUT_MODIFIER = new EventBus<>();

		private Events() {
		}
	}

	private final ControllerImpl controller;
	private final Controlify controlify;
	private final Minecraft minecraft;

	private double lookInputX, lookInputY; // in degrees per tick
	private final Vector3f gyroInput = new Vector3f();
	private boolean gyroToggledOn;
	private boolean wasAiming;
	private Animation flickAnimation;

	private boolean shouldShowPlayerList;

	public InGameInputHandler(ControllerImpl controller) {
		this.controller = controller;
		this.minecraft = Minecraft.getInstance();
		this.controlify = Controlify.instance();
		this.gyroToggledOn = false;
	}

	public void fixedUpdate() {
		boolean isController = ControllerPlayerMovement.shouldBeControllerInput();

		handlePlayerLookInput(isController);
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

		if (ControlifyActions.PAUSE.on(controller).pulse().consumePulse()) {
			minecraft.pauseGame(false);
		}
		if (minecraft.player != null) {
			Inventory inventory = minecraft.player.getInventory();

			if (ControlifyActions.NEXT_SLOT.on(controller).pulse().consumePulse()) {
				inventory.setSelectedSlot((inventory.getSelectedSlot() + 1) % Inventory.getSelectionSize());
			}
			if (ControlifyActions.PREV_SLOT.on(controller).pulse().consumePulse()) {
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
				if (hotbarSlots.get(i).on(controller).pulse().consumePulse()) {
					inventory.setSelectedSlot(i);
				}
			}

			if (!minecraft.player.isSpectator()) {
				if (ControlifyActions.DROP_STACK.on(controller).pulse().consumePulse()) {
					dropItem(minecraft.player, true);
				} else if (ControlifyActions.DROP_INGAME.on(controller).pulse().consumePulse()) {
					dropItem(minecraft.player, false);
				}

				if (ControlifyActions.SWAP_HANDS.on(controller).pulse().consumePulse()) {
					minecraft.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
				}
			}

			if (ControlifyActions.INVENTORY.on(controller).pulse().consumePulse()) {
				if (minecraft.gameMode.isServerControlledInventory()) {
					minecraft.player.sendOpenInventory();
				} else {
					minecraft.getTutorial().onOpenInventory();
					MinecraftUtil.setScreen(new InventoryScreen(minecraft.player));
				}
			}

			if (ControlifyActions.CHANGE_PERSPECTIVE.on(controller).pulse().consumePulse()) {
				CameraType cameraType = minecraft.options.getCameraType();
				minecraft.options.setCameraType(minecraft.options.getCameraType().cycle());
				if (cameraType.isFirstPerson() != minecraft.options.getCameraType().isFirstPerson()) {
					minecraft.gameRenderer.checkEntityPostEffect(minecraft.options.getCameraType().isFirstPerson() ? minecraft.getCameraEntity() : null);
				}

				//? if <26.2
				//minecraft.levelRenderer.needsUpdate();
			}
		}
		if (ControlifyActions.TOGGLE_HUD_VISIBILITY.on(controller).pulse().consumePulse()) {
			//? if >=26.2 {
			minecraft.gui.hud.toggle();
			//?} else {
			/*minecraft.options.hideGui = !minecraft.options.hideGui;
			*///?}
		}

		if (ControlifyActions.SHOW_PLAYER_LIST.on(controller).pulse().consumePulse()) {
			shouldShowPlayerList = !shouldShowPlayerList;
		}

		if (ControlifyActions.TOGGLE_DEBUG_MENU.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleOverlay();
		}
		if (ControlifyActions.TOGGLE_DEBUG_MENU_FPS.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleFpsOverlay();
		}
		if (ControlifyActions.TOGGLE_DEBUG_MENU_NET.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleNetworkOverlay();
		}
		if (ControlifyActions.TOGGLE_DEBUG_MENU_PROF.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleProfilerOverlay();
		}
		if (ControlifyActions.DEBUG_RELOAD_CHUNKS.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.reloadChunks();
		}
		if (ControlifyActions.DEBUG_TOGGLE_CHUNK_BORDERS.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleChunkBorders();
		}
		if (ControlifyActions.DEBUG_TOGGLE_ADVANCED_TOOLTIPS.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleAdvancedTooltips();
		}
		if (ControlifyActions.DEBUG_TOGGLE_ENTITY_HITBOXES.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.toggleEntityHitboxes();
		}
		if (ControlifyActions.DEBUG_RELOAD_RESOURCE_PACKS.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.reloadResourcePacks();
		}
		if (ControlifyActions.DEBUG_CLEAR_CHAT.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.clearChat();
		}
		if (ControlifyActions.DEBUG_START_STOP_PROFILING.on(controller).pulse().consumePulse()) {
			DebugOverlayHelper.startStopProfiling();
		}
		if (ControlifyActions.DEBUG_RADIAL.on(controller).pulse().consumePulse()) {
			MinecraftUtil.setScreen(new RadialMenuScreen(
					controller,
					ControlifyActions.DEBUG_RADIAL,
					RadialItems.createDebug(controller),
					Component.empty(),
					null
			));
		}

		if (ControlifyActions.TAKE_SCREENSHOT.on(controller).pulse().consumePulse()) {
			// get file before it takes and writes the screenshot (which changes the next name)
			File screenshotFile = ScreenshotAccessor.controlify$invokeGetFile(
					new File(minecraft.gameDirectory, "screenshots")
			);

			Screenshot.grab(
					this.minecraft.gameDirectory,
					//? if >=26.2 {
					this.minecraft.gameRenderer.mainRenderTarget(),
					//?} else {
					/*this.minecraft.getMainRenderTarget(),
					*///?}
					component -> this.minecraft.execute(() -> {
						//? if >=26.2 {
						this.minecraft.gui.hud.getChat()
						//?} else {
						/*this.minecraft.gui.getChat()
						*///?}
								.addClientSystemMessage(component);

						// TODO: this currently does not work, yet to debug why not
						SteamDeckDriver.getDeck().ifPresent(deck -> {
							deck.doSteamScreenshot(screenshotFile.getAbsoluteFile().toPath(), "");
						});
					})
			);
		}

		if (ControlifyActions.PICK_BLOCK.on(controller).pulse().consumePulse()) {
			((PickBlockAccessor) minecraft).controlify$pickBlock();
		}
		if (ControlifyActions.PICK_BLOCK_NBT.on(controller).pulse().consumePulse()) {
			((PickBlockAccessor) minecraft).controlify$pickBlockWithNbt();
		}

		if (ControlifyActions.RADIAL_MENU.on(controller).pulse().consumePulse()) {
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

		if (ControlifyActions.GAME_MODE_SWITCHER.on(controller).pulse().consumePulse() && canSwitchGameMode()) {
			MinecraftUtil.setScreen(new RadialMenuScreen(
					controller,
					ControlifyActions.GAME_MODE_SWITCHER,
					RadialItems.createGameModes().items(),
					Component.empty(),
					null)
			);
		}

		if (ControlifyActions.HOTBAR_SLOT_SELECT.on(controller).pulse().consumePulse()) {
			MinecraftUtil.setScreen(new RadialMenuScreen(
					controller,
					ControlifyActions.HOTBAR_SLOT_SELECT,
					RadialItems.createHotbarItemSelect(),
					Component.empty(),
					null
			));
		}

		if (minecraft.player != null && minecraft.player.hasInfiniteMaterials()) {
			if (ControlifyActions.HOTBAR_LOAD_RADIAL.on(controller).pulse().consumePulse()) {
				MinecraftUtil.setScreen(new RadialMenuScreen(
						controller,
						ControlifyActions.HOTBAR_LOAD_RADIAL,
						RadialItems.createHotbarLoad().items(),
						Component.translatable("controlify.radial.hotbar_load_hint"),
						null
				));
			}
			if (ControlifyActions.HOTBAR_SAVE_RADIAL.on(controller).pulse().consumePulse()) {
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

	protected void handlePlayerLookInput(boolean isController) {
		LocalPlayer player = this.minecraft.player;

		if (!isController || !canProcessLookInput()) {
			lookInputX = 0;
			lookInputY = 0;
			return;
		}

		boolean aiming = isAiming(player);

		Vector2d lookImpulse = new Vector2d();
		controller.gyro().ifPresent(gyro -> handleGyroLook(gyro, lookImpulse, aiming));

		if (controller.gyro().map(gyro -> gyro.settings().lookSensitivity > 0 && gyro.settings().flickStick).orElse(false)) {
			handleFlickStick(player);
		} else {
			handleRegularLook(controller.input(), lookImpulse, aiming, player);
		}

		var modifier = new LookInputModifier(new Vector2f((float) lookImpulse.x, (float) lookImpulse.y), controller);
		Events.LOOK_INPUT_MODIFIER.emit(modifier);
		lookImpulse.set(modifier.lookInput());

		lookInputX = lookImpulse.x;
		lookInputY = lookImpulse.y;

		wasAiming = aiming;
	}

	protected void handleRegularLook(InputComponentImpl input, Vector2d impulse, boolean aiming, LocalPlayer player) {
		InputSettings settings = input.settings();

		// normal look input
		Vector2d regularImpulse = new Vector2d(
				ControlifyActions.LOOK_RIGHT.on(controller).continuous().getContinuous()
						- ControlifyActions.LOOK_LEFT.on(controller).continuous().getContinuous(),
				ControlifyActions.LOOK_DOWN.on(controller).continuous().getContinuous()
						- ControlifyActions.LOOK_UP.on(controller).continuous().getContinuous()
		);
		if (settings.sensitivity.hLookInvert) {
			regularImpulse.x *= -1;
		}
		if (settings.sensitivity.vLookInvert) {
			regularImpulse.y *= -1;
		}

		if (!settings.sensitivity.isLCE) {
			// apply the easing on its length to preserve circularity
			regularImpulse = ControllerUtils.applyEasingToLength(
					regularImpulse,
					settings.sensitivity.lookInputCurve::apply
			);
		} else {
			// LCE uses a quadratic curve on each axis independently,
			// and its default turn speed was 7.5 degrees per tick at 100% sensitivity
			regularImpulse.x = regularImpulse.x * Math.abs(regularImpulse.x) * 0.75f;
			regularImpulse.y = regularImpulse.y * Math.abs(regularImpulse.y) * 0.75f;
		}

		if (settings.sensitivity.reduceAimingSensitivity && player.isUsingItem()) {
			float aimMultiplier = settings.sensitivity.isLCE
					? switch (player.getUseItem().getUseAnimation()) {
						case BOW, CROSSBOW, SPEAR, SPYGLASS -> 0.15f;
						default -> 1f;
					}
					: switch (player.getUseItem().getUseAnimation()) {
						case BOW, CROSSBOW, SPEAR -> 0.6f;
						case SPYGLASS -> 0.2f;
						default -> 1f;
					};
			regularImpulse.mul(aimMultiplier);
		}

		// 10 degrees per tick at 100% sensitivity
		regularImpulse.x *= settings.sensitivity.hLookSensitivity * 10f;
		regularImpulse.y *= settings.sensitivity.vLookSensitivity * 10f;

		impulse.add(regularImpulse);
	}

	protected void handleGyroLook(GyroComponentImpl gyro, Vector2d impulse, boolean aiming) {
		GyroSettings settings = gyro.settings();
		var gyroButton = ControlifyActions.GYRO_BUTTON.on(controller);

		if (settings.buttonMode.equals(GyroButtonMode.ON) && (!gyroButton.latch().isLatchActive() && !aiming)) {
			gyroInput.set(0);
		} else if(settings.buttonMode.equals(GyroButtonMode.INVERT) && (gyroButton.latch().isLatchActive() && !aiming)) {
			gyroInput.set(0);
		} else if(settings.buttonMode.equals(GyroButtonMode.TOGGLE) && (!gyroToggledOn && !aiming)) {
			gyroInput.set(0);
		} else {
			if (settings.relativeMode) {
				gyroInput.add(new Vector3f(gyro.getState()).mul(0.1f));
			}
			else
				gyroInput.set(gyro.getState());
		}

		boolean gyroPressed = gyroButton.pulse().consumePulse();
		if (settings.buttonMode.equals(GyroButtonMode.TOGGLE) && gyroPressed) {
			gyroToggledOn = !gyroToggledOn;
		}

		// convert radians per second into degrees per tick
		var thisInput = new Vector3f(gyroInput)
				.mul(Mth.RAD_TO_DEG)
				.div(20)
				.mul(settings.lookSensitivity);

		impulse.y += -thisInput.x() * (settings.invertPitch ? -1 : 1);
		impulse.x += switch (settings.yawMode) {
			case YAW -> -thisInput.y();
			case ROLL -> -thisInput.z();
			case BOTH -> -thisInput.y() - thisInput.z();
		} * (settings.invertYaw ? -1 : 1);
	}

	protected void handleFlickStick(LocalPlayer player) {
		float y = ControlifyActions.LOOK_DOWN.on(controller).continuous().getContinuous()
				- ControlifyActions.LOOK_UP.on(controller).continuous().getContinuous();
		float x = ControlifyActions.LOOK_RIGHT.on(controller).continuous().getContinuous()
				- ControlifyActions.LOOK_LEFT.on(controller).continuous().getContinuous();

		float flickAngle = Mth.wrapDegrees((float) Mth.atan2(y, x) * Mth.RAD_TO_DEG + 90f);

		if (!ControlifyActions.LOOK_DOWN.on(controller).pulse().consumePulse()
				& !ControlifyActions.LOOK_UP.on(controller).pulse().consumePulse()
				& !ControlifyActions.LOOK_LEFT.on(controller).pulse().consumePulse()
				& !ControlifyActions.LOOK_RIGHT.on(controller).pulse().consumePulse()
		) {
			return;
		}

		if (flickAnimation != null && flickAnimation.isPlaying()) {
			flickAnimation.skipToEnd();
		}

		flickAnimation = Animation.of(8)
				.easing(EasingFunction.EASE_OUT_EXPO)
				.deltaConsumerD(angle -> player.turn(angle, 0), 0, flickAngle / 0.15)
				.play();
	}

	public void processPlayerLook(float deltaTime) {
		if (minecraft.player != null) {
			double velX = lookInputX / 0.15 * deltaTime;
			double velY = lookInputY / 0.15 * deltaTime;
			//noinspection SuspiciousNameCombination
			minecraft.player.turn(velX, velY);
			minecraft.getTutorial().onMouse(velX, velY);
		}
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

	private boolean isAiming(Player player) {
		return player.isUsingItem() && switch (player.getUseItem().getUseAnimation()) {
			case BOW, CROSSBOW, SPEAR, SPYGLASS -> true;
			default -> false;
		};
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
