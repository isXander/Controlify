/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify;

import com.mojang.blaze3d.Blaze3D;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.contextual.ContextualDomainRegistry;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.bindings.ControlifyBindApiImpl;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.action.radial.RadialIconManager;
import dev.isxander.controlify.action.defaults.DefaultBindManager;
import dev.isxander.controlify.compatibility.ControlifyCompat;
import dev.isxander.controlify.config.ConfigManager;
import dev.isxander.controlify.config.dto.profile.defaults.DefaultConfigManager;
import dev.isxander.controlify.contextual.ContextualDomains;
import dev.isxander.controlify.contextual.RuleSetManager;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.dualsense.TriggerEffectManager;
import dev.isxander.controlify.controller.id.ControllerTypeManager;
import dev.isxander.controlify.input.mapping.ControllerMappingManager;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controllermanager.ControllerCoordinator;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.driver.steamdeck.SteamDeckMode;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.font.InputFontMapper;
import dev.isxander.controlify.gui.screen.*;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.action.activator.ActivatorConfigManager;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.platform.network.SidedNetworkApi;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.screenop.keyboard.KeyboardLayoutManager;
import dev.isxander.controlify.server.*;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import dev.isxander.controlify.server.packets.*;
import dev.isxander.controlify.sound.ControlifyClientSounds;
import dev.isxander.controlify.utils.*;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import dev.isxander.controlify.wireless.LowBatteryNotifier;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.IOException;
import java.util.*;

import static dev.isxander.controlify.utils.ControllerUtils.wrapControllerError;

public class Controlify implements ControlifyApi {
	private static Controlify instance = null;

	private Minecraft minecraft = null;

	private ControllerCoordinator controllerCoordinator;
	private InputMode currentInputMode = InputMode.KEYBOARD_MOUSE;

	private ConfigManager config;

	private @Nullable InGameInputHandler inGameInputHandler;
	public @Nullable InGameButtonGuide inGameButtonGuide;
	private VirtualMouseHandler virtualMouseHandler;

	// Asset reloaders / managers
	private InputFontMapper inputFontMapper;
	private DefaultBindManager defaultBindManager;
	private DefaultConfigManager defaultConfigManager;
	private ControllerTypeManager controllerTypeManager;
	private KeyboardLayoutManager keyboardLayoutManager;
	private RadialIconManager radialIconManager;
	private ActivatorConfigManager activatorConfigManager;
	private ControllerMappingManager controllerMappingManager;

	private TriggerEffectManager triggerEffectManager;

	private SetupWizardManager setupWizardManager;

	private int consecutiveInputSwitches = 0;
	private double lastInputSwitchTime = 0;

	private int showMouseTicks = 0;
	private boolean mouseUsedThisFrame = false;

	/**
	 * Called at usual fabric client entrypoint and in NeoForge mod constructor
	 * Always runs, even with no controllers detected.
	 * In this state, Controlify is only partially loaded, no controllers
	 * have been initialised, nor has the config. This is done at {@link Controlify#initializeControlify()}.
	 * This is where regular fabric callbacks and forge events should be registered.
	 * On NeoForge, this is run so early that Minecraft.getInstance() has not yet been set. So extra care should be taken as to not use it.
	 */
	public void preInitialiseControlify() {
		DebugProperties.printProperties();

		CUtil.LOGGER.log("Pre-initializing Controlify...");

		if (DebugProperties.MIXIN_AUDIT) {
			MixinEnvironment.getCurrentEnvironment().audit();
		}

		this.config = new ConfigManager(
				PlatformMainUtil.getConfigDir()
		);
		PlatformClientUtil.registerClientStopping(_ -> {
			this.getControllerCoordinator().ifPresent(ControllerCoordinator::close);
			this.config.close();
		});

		this.setupWizardManager = new SetupWizardManager();

		this.inputFontMapper = new InputFontMapper();
		this.defaultBindManager = new DefaultBindManager();
		this.defaultConfigManager = new DefaultConfigManager();
		this.controllerTypeManager = new ControllerTypeManager();
		this.keyboardLayoutManager = new KeyboardLayoutManager();
		this.radialIconManager = new RadialIconManager();
		this.activatorConfigManager = new ActivatorConfigManager();
		this.controllerMappingManager = new ControllerMappingManager();

		PlatformClientUtil.registerAssetReloadListener(this.inputFontMapper);
		PlatformClientUtil.registerAssetReloadListener(this.defaultBindManager);
		PlatformClientUtil.registerAssetReloadListener(this.defaultConfigManager);
		PlatformClientUtil.registerAssetReloadListener(this.controllerTypeManager);
		PlatformClientUtil.registerAssetReloadListener(this.keyboardLayoutManager);
		PlatformClientUtil.registerAssetReloadListener(this.radialIconManager);
		PlatformClientUtil.registerAssetReloadListener(this.activatorConfigManager);
		PlatformClientUtil.registerAssetReloadListener(this.controllerMappingManager);
		PlatformClientUtil.registerAssetReloadListener(RuleSetManager.GUIDE_RULES);
		PlatformClientUtil.registerAssetReloadListener(RuleSetManager.TRIGGER_EFFECT_RULES);
		ContextualDomains.touch();

		registerBuiltinPack("legacy_console");

		ControlifyClientSounds.init();

		ControlifyHandshake.setupOnClient();

		SidedNetworkApi.S2C().<VibrationPacket>listenForPacket(VibrationPacket.CHANNEL, packet -> {
			if (config().getSettings().globalSettings().allowServerRumble) {
				getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
						rumble.rumbleManager().play(packet.source(), packet.createEffect()));
			}
		});
		SidedNetworkApi.S2C().<OriginVibrationPacket>listenForPacket(OriginVibrationPacket.CHANNEL, packet -> {
			if (config().getSettings().globalSettings().allowServerRumble) {
				getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
						rumble.rumbleManager().play(packet.source(), packet.createEffect()));
			}
		});
		SidedNetworkApi.S2C().<EntityVibrationPacket>listenForPacket(EntityVibrationPacket.CHANNEL, packet -> {
			if (config().getSettings().globalSettings().allowServerRumble) {
				getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
						rumble.rumbleManager().play(packet.source(), packet.createEffect()));
			}
		});
		SidedNetworkApi.S2C().<ServerPolicyPacket>listenForPacket(ServerPolicyPacket.CHANNEL, packet -> {
			CUtil.LOGGER.log("Connected server specified '{}' policy is {}.", packet.id(), packet.allowed() ? "ALLOWED" : "DISALLOWED");
			ServerPolicies policy = ServerPolicies.getById(packet.id());
			if (policy == null) {
				CUtil.LOGGER.warn("Connected server specified unknown '{}' policy; ignoring it.", packet.id());
				return;
			}
			policy.set(ServerPolicy.fromBoolean(packet.allowed()));
		});

		PlatformClientUtil.registerClientDisconnected(_ -> {
			DebugLog.log("Disconnected from server, resetting server policies");
			ServerPolicies.unsetAll();
		});
		PlatformClientUtil.registerClientTagsUpdated(_ -> {
			ContextualDomains.INSTANCE.invalidateResolvedFactGraphs();
			RuleSetManager.GUIDE_RULES.invalidateResolvedRuleEngines();
			RuleSetManager.TRIGGER_EFFECT_RULES.invalidateResolvedRuleEngines();
		});

		PlatformClientUtil.addHudLayer(CUtil.rl("button_guide"), (graphics, deltaTracker) ->
				inGameButtonGuide().ifPresent(guide -> guide.extractRenderState(graphics, deltaTracker.getGameTimeDeltaPartialTick(false))));

		PlatformMainUtil.applyToControlifyEntrypoint(entrypoint -> {
			try {
				entrypoint.onControlifyPreInit(new PreInitContext() {
					@Override
					public ControlifyBindApi bindings() {
						return ControlifyBindApiImpl.INSTANCE;
					}

					@Override
					public ContextualDomainRegistry contextualDomains() {
						return ContextualDomains.INSTANCE;
					}
				});
			} catch (Throwable e) {
				CUtil.LOGGER.error("Failed to run `onControlifyPreInit` on Controlify entrypoint: {}", entrypoint.getClass().getName(), e);
			}
		});
	}

	private void registerBuiltinPack(String id) {
		PlatformClientUtil.registerBuiltinResourcePack(
				CUtil.rl(id),
				Component.translatable("controlify.extra_pack." + id + ".name")
		);
	}

	/**
	 * Called once Minecraft has completely loaded.
	 * (When the loading overlay starts to fade).
	 *
	 * This is where controllers are usually initialised, as long
	 * as one or more controllers are connected.
	 */
	public void initializeControlify() {
		CUtil.LOGGER.log("Initializing Controlify...");
		this.minecraft = Minecraft.getInstance();

		config().loadOrDefault();

		this.inGameInputHandler = null; // set when the current controller changes
		this.virtualMouseHandler = new VirtualMouseHandler();
		this.triggerEffectManager = new TriggerEffectManager();

		ControlifyBindings.registerModdedBindings();

		PlatformClientUtil.registerPostScreenRender((screen, graphics, mouseX, mouseY, tickDelta) ->
				ControlifyApi.get().getCurrentController().ifPresent(controller -> {
					virtualMouseHandler().renderVirtualMouse(graphics);
					ScreenProcessorProvider.provide(screen).render(controller, graphics, tickDelta);
				}));

		ControlifyEvents.CURRENT_CONTROLLER_CHANGED.register(event ->
				this.onCurrentControllerChanged(event.controller(), event.changeInputMode()));
		this.controllerCoordinator = new ControllerCoordinator(this, this.setupWizardManager);
		if (!this.controllerCoordinator.initialize()) {
			return;
		}

		PlatformClientUtil.registerClientTickStarted(this::fixedUpdate);

		// initialise and compatability modules that controlify implements itself
		// this does NOT invoke any entrypoints. this is done in the pre-initialisation phase
		ControlifyCompat.init();

		// make sure people don't someone add binds after controllers could have been created
		ControlifyBindApiImpl.INSTANCE.lock();

		discoverControllers();

		if (DebugProperties.INIT_DUMP) {
			CUtil.LOGGER.log("\n{}", DebugDump.dumpDebug());
		}

		if (this.config().getSettings().globalSettings().useEnhancedSteamDeckDriver) {
			doSteamDeckChecks();
		}

		PlatformMainUtil.applyToControlifyEntrypoint(entrypoint -> {
			try {
				entrypoint.onControlifyInit(new InitContext() {
					@Override
					public ControlifyApi controlify() {
						return Controlify.this;
					}
				});
			} catch (Throwable e) {
				CUtil.LOGGER.error("Failed to run `onControlifyInit` on Controlify entrypoint: {}", entrypoint.getClass().getName(), e);
			}
		});
	}

	private void doSteamDeckChecks() {
		CUtil.LOGGER.log("Steam Deck state: {}", SteamDeckUtil.DECK_MODE);

		if (!SteamDeckUtil.IS_STEAM_DECK && DebugProperties.STEAM_DECK_CUSTOM_CEF_URL == null) {
			return;
		}

		boolean connectedToCef = SteamDeckUtil.getDeckInstance().isPresent();

		if (!connectedToCef) {
			CUtil.LOGGER.error("Controlify could not connect to CEF debugger instance. Decky is probably not installed.");
			InitialScreenRegistryDuck.registerInitialScreen(SteamDeckAlerts::createDeckyRequiredWarning);
		}

		if (SteamDeckUtil.DECK_MODE == SteamDeckMode.DESKTOP_MODE) {
			CUtil.LOGGER.warn("Controlify is running in SteamOS desktop mode.");
			InitialScreenRegistryDuck.registerInitialScreen(SteamDeckAlerts::createDesktopModeWarning);
		}

		if (connectedToCef && SteamDeckUtil.DECK_MODE == SteamDeckMode.GAMING_MODE) {
			CUtil.LOGGER.log("Steam Deck is in gaming mode and Controlify has successfully connected to CEF.");
		}
	}

	/// Discovers already-connected controllers once the backend is initialized.
	public void discoverControllers() {
		this.getControllerCoordinator().ifPresent(ControllerCoordinator::discoverControllers);
	}

	/// Called once per frame, just after Minecraft has done its own SDL event polling.
	public void update(float tickDelta) {
		if (this.minecraft == null || this.controllerCoordinator == null || !this.controllerCoordinator.isReady()) {
			return;
		}

		boolean outOfFocus = !config().getSettings().globalSettings().outOfFocusInput
				&& !this.minecraft.isWindowActive();
		boolean givingInput = this.controllerCoordinator.update(outOfFocus);

		this.setupWizardManager.tick();

		// handle showing/hiding mouse whilst in mixed input mode
		if (this.minecraft.mouseHandler.isMouseGrabbed()) {
			showMouseTicks = 0;
		}
		this.getCurrentController().ifPresent(currentController -> {
			wrapControllerError(
					() -> updateActiveController(currentController, givingInput),
					"Updating current controller",
					currentController
			);
		});

		this.mouseUsedThisFrame = false;
	}

	/// Called at 20tps
	public void fixedUpdate() {
		LowBatteryNotifier.tick();

		if (currentInputMode() == InputMode.MIXED && showMouseTicks > 0) {
			showMouseTicks--;
			if (showMouseTicks == 0) {
				restoreVirtualCursor();
			}
		}

		boolean outOfFocus = !config().getSettings().globalSettings().outOfFocusInput
							 && !this.minecraft.isWindowActive();

		this.getCurrentController().ifPresent(currentController -> {
			wrapControllerError(
					() -> fixedUpdateActiveController(currentController, outOfFocus),
					"Fixed-updating current controller",
					currentController
			);
		});
	}

	private void updateActiveController(ControllerEntity controller, boolean givingInput) {
		if (givingInput) {
			minecraft.getFramerateLimitTracker().onInputReceived();

			if (showMouseTicks > 0 && !mouseUsedThisFrame) {
				this.endTemporaryCursor();
			}

			if (!this.currentInputMode().isController()) {
				this.setInputMode(config().getSettings().globalSettings().mixedInput ? InputMode.MIXED : InputMode.CONTROLLER);

				return; // don't process input if this is changing mode.
			}
		}

		if (consecutiveInputSwitches > 100) {
			CUtil.LOGGER.warn("Controlify detected current controller to be constantly giving input and has been disabled.");
			MinecraftUtil.sendToast(
					Component.translatable("controlify.toast.faulty_input.title"),
					Component.translatable("controlify.toast.faulty_input.description"),
					true
			);
			this.setCurrentController(null, true);
			consecutiveInputSwitches = 0;
			return;
		}

		if (this.currentInputMode().isController()) {
			ControlifyEvents.ACTIVE_CONTROLLER_TICKED.invoke(new ControlifyEvents.ControllerStateUpdate(controller));
		}
	}

	private void fixedUpdateActiveController(ControllerEntity controller, boolean outOfFocus) {
		boolean controllerInputSuppressed = outOfFocus || currentInputMode() == InputMode.KEYBOARD_MOUSE;
		this.triggerEffectManager().tick(this.minecraft, controller);
		this.triggerEffectManager().applyTriggerEffects(controller, controllerInputSuppressed);

		boolean isPaused = minecraft.isPaused() || MinecraftUtil.getScreen() instanceof PauseScreen;
		boolean isConfigScreen = MinecraftUtil.getScreen() instanceof YACLScreen;

		Optional<RumbleManager> rumbleManager = controller.rumble().map(RumbleComponent::rumbleManager);
		rumbleManager.ifPresent(rumble -> rumble.setSilent(outOfFocus || (isPaused && !isConfigScreen) || currentInputMode() == InputMode.KEYBOARD_MOUSE));
		if (!outOfFocus) {
			rumbleManager.ifPresent(RumbleManager::tick);
		}

		if (minecraft.level != null) {
			this.inGameInputHandler().ifPresent(InGameInputHandler::fixedUpdate);
		}

		if (this.currentInputMode().isController()) {
			if (MinecraftUtil.getScreen() != null) {
				ScreenProcessorProvider.provide(MinecraftUtil.getScreen()).onControllerFixedUpdate(controller);
			}
		}
	}

	public void applyControllerSelection(boolean changeInputMode) {
		this.getControllerCoordinator().ifPresent(coordinator -> coordinator.applyControllerSelection(changeInputMode));
	}

	public boolean switchProfile(int profileIndex, boolean remember) throws IOException {
		if (!config().switchProfile(profileIndex, remember)) {
			return false;
		}

		this.getControllerCoordinator().ifPresent(ControllerCoordinator::applyActiveProfile);
		this.setupForController(this.currentInputMode.isController() ? this.getCurrentController().orElse(null) : null);
		return true;
	}

	public ConfigManager config() {
		return config;
	}

	@Override
	public @NotNull Optional<ControllerEntity> getCurrentController() {
		return this.getControllerCoordinator().flatMap(ControllerCoordinator::currentController);
	}

	public void setCurrentController(@Nullable ControllerEntity controller, boolean changeInputMode) {
		this.getControllerCoordinator().ifPresent(coordinator -> coordinator.setCurrentController(controller, changeInputMode));
	}

	private void onCurrentControllerChanged(@Nullable ControllerEntity controller, boolean changeInputMode) {
		boolean changedInputMode = false;
		if (controller == null) {
			changedInputMode = this.setInputMode(InputMode.KEYBOARD_MOUSE);
		} else if (changeInputMode) {
			changedInputMode = this.setInputMode(config().getSettings().globalSettings().mixedInput ? InputMode.MIXED : InputMode.CONTROLLER);
		}
		if (!changedInputMode) {
			this.setupForController(this.currentInputMode.isController() ? controller : null);
		}
	}

	@Override
	public boolean setInputMode(@NotNull InputMode newInputMode) {
		if (this.currentInputMode == newInputMode) return false;
		if (newInputMode.isController() && this.getCurrentController().isEmpty()) {
			DebugLog.log("Attempted to switch to controller input mode with no current controller set.");
			return false;
		}

		this.currentInputMode = newInputMode;

		// Track consecutive input mode switches to prevent softlock
		if (Blaze3D.getTime() - lastInputSwitchTime < 20) {
			consecutiveInputSwitches++;
		} else {
			consecutiveInputSwitches = 0;
		}
		lastInputSwitchTime = Blaze3D.getTime();

		if (!minecraft.mouseHandler.isMouseGrabbed()) {
			if (newInputMode == InputMode.KEYBOARD_MOUSE && virtualMouseHandler().isVirtualMouseEnabled()) {
				moveCursorToVirtualMouse();
			}
			hideMouse(newInputMode.isController(), true);
		}

		this.setupForController(this.currentInputMode.isController() ? this.getCurrentController().orElse(null) : null);

		KeyMapping.resetToggleKeys();

		// If we have already joined a server with KB&M, then switch to controller,
		// we should do the new server notification as it won't have been triggered on join.
		if (this.currentInputMode.isController()) {
			if (minecraft.getCurrentServer() != null) {
				notifyNewServer(minecraft.getCurrentServer());
			}
		}

		// notify current screen of input mode change
		if (MinecraftUtil.getScreen() != null) {
			ScreenProcessorProvider.provide(MinecraftUtil.getScreen()).onInputModeChanged(newInputMode);
		}

		// notify event listeners of input mode change
		ControlifyEvents.INPUT_MODE_CHANGED.invoke(new ControlifyEvents.InputModeChanged(newInputMode));

		return true;
	}

	private void setupForController(@Nullable ControllerEntity controller) {
		ControllerPlayerMovement.updatePlayerInput(minecraft.player);

		if (controller == null) {
			this.inGameInputHandler = null;
			this.inGameButtonGuide = null;
			return;
		}

		this.inGameInputHandler = new InGameInputHandler(controller);
		this.inGameButtonGuide = new InGameButtonGuide(controller, this.minecraft);

		controller.input().ifPresent(InputComponent::resetActions);
	}

	public Optional<ControllerManager> getControllerManager() {
		return this.getControllerCoordinator().map(ControllerCoordinator::controllerManager);
	}

	public Optional<ControllerCoordinator> getControllerCoordinator() {
		return Optional.ofNullable(this.controllerCoordinator);
	}

	public Optional<InGameInputHandler> inGameInputHandler() {
		return Optional.ofNullable(inGameInputHandler);
	}

	public Optional<InGameButtonGuide> inGameButtonGuide() {
		return Optional.ofNullable(inGameButtonGuide);
	}

	public VirtualMouseHandler virtualMouseHandler() {
		return virtualMouseHandler;
	}

	public @NotNull InputMode currentInputMode() {
		return currentInputMode;
	}

	public void hideMouse(boolean hide, boolean moveMouse) {
		CursorUtils.setVisibility(minecraft.getWindow(), !hide);

		if (MinecraftUtil.getScreen() != null) {
			var mouseHandlerAccessor = (MouseHandlerAccessor) minecraft.mouseHandler;
			if (hide && !virtualMouseHandler().isVirtualMouseEnabled() && !virtualMouseHandler().requiresVirtualMouse() && moveMouse) {
				long handle = minecraft.getWindow().handle();
				// stop mouse hovering over last element before hiding cursor but don't actually move it
				// so when the user switches back to mouse it will be in the same place
				mouseHandlerAccessor.controlify$invokeOnMove(handle, -50, -50 /*? if >=26.3 {*/, 0, 0/*?}*/);
			}
		}
	}

	public void showCursorTemporarily() {
		if (currentInputMode() == InputMode.MIXED && !minecraft.mouseHandler.isMouseGrabbed()) {
			mouseUsedThisFrame = true;
			if (virtualMouseHandler().isVirtualMouseEnabled()) {
				moveCursorToVirtualMouse();
				virtualMouseHandler().disableVirtualMouse();
			}
			hideMouse(false, false);
			showMouseTicks = 20 * 2;
		}
	}

	private void moveCursorToVirtualMouse() {
		CursorUtils.setPosition(
			minecraft.getWindow(),
			virtualMouseHandler().getCurrentX(0),
			virtualMouseHandler().getCurrentY(0)
		);
	}

	public void endTemporaryCursor() {
		if (currentInputMode() != InputMode.MIXED || showMouseTicks <= 0) return;

		showMouseTicks = 0;
		restoreVirtualCursor();
	}

	private void restoreVirtualCursor() {
		hideMouse(true, false);
		if (virtualMouseHandler().requiresVirtualMouse()) {
			virtualMouseHandler().enableVirtualMouse();
		}
	}

	public InputFontMapper inputFontMapper() {
		return this.inputFontMapper;
	}

	public DefaultBindManager defaultBindManager() {
		return this.defaultBindManager;
	}

	public DefaultConfigManager defaultConfigManager() {
		return this.defaultConfigManager;
	}

	public ControllerTypeManager controllerTypeManager() {
		return this.controllerTypeManager;
	}

	public KeyboardLayoutManager keyboardLayoutManager() {
		return this.keyboardLayoutManager;
	}

	public RadialIconManager radialIconManager() {
		return this.radialIconManager;
	}

	public ActivatorConfigManager activatorConfigManager() {
		return this.activatorConfigManager;
	}

	public ControllerMappingManager controllerMappingManager() {
		return this.controllerMappingManager;
	}

	public TriggerEffectManager triggerEffectManager() {
		return this.triggerEffectManager;
	}

	public void notifyNewServer(ServerData data) {
		if (!currentInputMode().isController())
			return;

		if (config().getSettings().globalSettings().seenServers.add(data.ip)) {
			MinecraftUtil.sendToast(
					Component.translatable("controlify.toast.new_server.title"),
					Component.translatable("controlify.toast.new_server.description", data.name),
					true
			);
			config().saveSafely();
		}
	}

	public static Controlify instance() {
		if (instance == null) instance = new Controlify();
		return instance;
	}
}
