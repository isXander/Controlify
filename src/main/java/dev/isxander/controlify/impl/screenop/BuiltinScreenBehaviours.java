/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.gui.keyboard.*;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.compatibility.ControlifyCompat;
import dev.isxander.controlify.impl.gui.controllers.BindController;
import dev.isxander.controlify.impl.gui.keyboard.*;
import dev.isxander.controlify.impl.gui.screen.*;
import dev.isxander.controlify.impl.screenop.behaviours.element.*;
import dev.isxander.controlify.impl.screenop.behaviours.screen.*;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.*;
import dev.isxander.controlify.impl.screenop.behaviours.virtualmouse.*;
import dev.isxander.controlify.mixins.feature.screenop.impl.container.CreativeModeInventoryScreenMixin;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.*;
import dev.isxander.controlify.mixins.feature.screenop.runtime.SliderAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.multiplayer.*;
import net.minecraft.client.gui.screens.options.*;
import net.minecraft.client.gui.screens.worldselection.*;
import net.minecraft.client.input.KeyEvent;

import java.util.Optional;

/// Built-ins use the same registration events as addons. Priorities order whole
/// groups; explicit chains define ordering within each group.
public final class BuiltinScreenBehaviours {
	private BuiltinScreenBehaviours() {
	}

	public static void initialize() {
		ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribe(BuiltinScreenBehaviours::registerScreens);
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribe(BuiltinScreenBehaviours::registerElements);
		ControlifyCompat.initializeScreenBehaviours();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerScreens(ScreenBehaviourRegistry registry) {
		registry.register(Screen.class, 100, BuiltinScreenBehaviours::createCursorAndFocusBehaviours);
		registry.register(Screen.class, -100, BuiltinScreenBehaviours::createDefaultControls);
		registry.<TitleScreen>register(TitleScreen.class, TitleScreenBehaviour::new);
		registry.<SelectWorldScreen>register(SelectWorldScreen.class, SelectWorldScreenBehaviour::new);
		registry.<AbstractContainerScreen<?>>register((Class) AbstractContainerScreen.class, context -> {
			((ScreenContextImpl<?>) context).virtualMouse().setDefaultMode(VirtualMouseMode.CURSOR_SCROLL);
			return new ContainerScreenBehaviour(context);
		});
		registry.<AbstractRecipeBookScreen<?>>register((Class) AbstractRecipeBookScreen.class, RecipeBookScreenBehaviour::new);
		registry.<CreativeModeInventoryScreen>register(CreativeModeInventoryScreen.class, CreativeModeInventoryScreenBehaviour::new);
		registry.<OptionsScreen>register(OptionsScreen.class, DoneButtonGuideBehaviour::new);
		registry.<OptionsSubScreen>register(OptionsSubScreen.class, DoneButtonGuideBehaviour::new);
		registry.<PauseScreen>register(PauseScreen.class, PauseScreenBehaviour::new);
		registry.<JoinMultiplayerScreen>register(JoinMultiplayerScreen.class, JoinMultiplayerScreenBehaviour::new);
		registry.<DirectJoinServerScreen>register(DirectJoinServerScreen.class, ServerAddressBehaviour::new);
		registry.<ManageServerScreen>register(ManageServerScreen.class, ServerAddressBehaviour::new);
		registry.<CreateWorldScreen>register(CreateWorldScreen.class, CreateWorldScreenBehaviour::new);
		registry.<ChatScreen>register(ChatScreen.class, ChatScreenBehaviour::new);
		registry.<AbstractSignEditScreen>register(AbstractSignEditScreen.class, SignEditingBehaviour::new);
		registry.<BindConsumerScreen>register(BindConsumerScreen.class, 200, InputCaptureBehaviour::new);
		registry.<ControllerMappingMakerScreen>register(ControllerMappingMakerScreen.class, 200, InputCaptureBehaviour::new);
		registry.<RadialMenuScreen>register(RadialMenuScreen.class, RadialMenuScreen.Behaviour::new);
		registry.<RadialMenuEditScreen>register(RadialMenuEditScreen.class, 200, RadialMenuEditScreen.Behaviour::new);
		registry.<ControlifySettingsScreen>register(ControlifySettingsScreen.class, ControlifySettingsScreen.Behaviour::new);
		registry.<ProfileSelectionScreen>register(ProfileSelectionScreen.class, ProfileSelectionScreen.Behaviour::new);
		registry.<EnchantmentScreen>register(EnchantmentScreen.class, AdditionalSnapPointsBehaviour::new);
		registry.<LoomScreen>register(LoomScreen.class, AdditionalSnapPointsBehaviour::new);
		registry.<StonecutterScreen>register(StonecutterScreen.class, AdditionalSnapPointsBehaviour::new);
	}

	private static void registerElements(ScreenopEvents.RegisterElementBehavioursEvent event) {
		var registry = event.registry();
		var context = event.screenContext();

		// Snap eligibility -> custom behaviours (zero by default) -> traversal -> centre.
		registry.register(AbstractWidget.class, 100, DefaultWidgetBehaviour::new);
		registry.register(ContainerEventHandler.class, -100, DefaultContainerBehaviour::new);
		registry.register(AbstractWidget.class, -200, ElementBehaviours::snapPoint);
		registry.register(AbstractButton.class, ElementBehaviours.enabledWidget(ElementBehaviours.button(
				button -> button.onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0)),
				button -> button.playDownSound(Minecraft.getInstance().getSoundManager())
		)));
		registry.register(AbstractSliderButton.class, ElementBehaviours.enabledWidget(ElementBehaviours.slider(
				BuiltinScreenBehaviours::adjustSlider,
				slider -> ((SliderAccessor) slider).controlify$canChangeValue(),
				(slider, editing) -> ((SliderAccessor) slider).controlify$setCanChangeValue(editing)
		)));
		registry.register(EditBox.class, DefaultEditBoxBehaviour::new);
		registry.register(KeyWidget.class, key -> key);
		registry.register(KeyboardWidget.class, keyboard -> keyboard);

		registry.register(BindController.BindControllerElement.class, element -> element);
		registry.register(WorldSelectionList.WorldListEntry.class, ElementBehaviours.button(entry -> {
			if (context.screen() instanceof SelectWorldScreen screen) {
				screen.setFocused(((SelectWorldScreenAccessor) screen).controlify$getPlayWorldButton());
			}
		}));
		registry.register(ServerSelectionList.Entry.class, entry -> {
			if (entry instanceof ServerSelectionList.LANHeader) {
				return ElementBehaviours.chain();
			}
			return ElementBehaviours.<ServerSelectionList.Entry>button(ignored -> {
				if (context.screen() instanceof JoinMultiplayerScreen screen) {
					screen.setFocused(((JoinMultiplayerScreenAccessor) screen).controlify$getJoinButton());
				}
			}).create(entry);
		});
		registerLanguageEntries(registry);
		if (context.screen() instanceof ChatScreen) {
			registry.register(EditBox.class, 10, box -> new ElementBehaviour() {
				@Override
				public Optional<ElementKeyboardBehaviour> keyboardBehaviour(ScreenContext<?> screen) {
					return Optional.of(new ElementKeyboardBehaviour.DoNothing());
				}
			});
		}
		if (context.screen() instanceof ServerAddressScreenAccess address) {
			registry.register(EditBox.class, 10, box -> ElementBehaviours.conditional(
					() -> box == address.controlify$getAddressField(),
					new DefaultEditBoxBehaviour(box, KeyboardLayouts.SERVER_IP, new EditBoxInputTarget(box))
			));
		}
		if (context.screen() instanceof CreativeModeInventoryScreen screen) {
			var access = (CreativeModeInventoryScreenMixin) screen;
			registry.register(EditBox.class, 10, box -> {
				var input = new DelegatingInputTarget(new EditBoxInputTarget(box)) {
					@Override
					public boolean acceptChar(char ch, int modifiers) {
						boolean handled = super.acceptChar(ch, modifiers);
						access.controlify$refreshSearchResults();
						return handled;
					}

					@Override
					public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
						boolean handled = super.acceptKeyCode(keycode, scancode, modifiers);
						access.controlify$refreshSearchResults();
						return handled;
					}
				};
				return ElementBehaviours.conditional(() -> box == access.controlify$getSearchBox(),
						new DefaultEditBoxBehaviour(box, KeyboardLayouts.SIMPLE, input));
			});
		}
	}

	private static void registerLanguageEntries(ElementBehaviourRegistry registry) {
		try {
			var type = Class.forName("net.minecraft.client.gui.screens.options.LanguageSelectScreen$LanguageSelectionList$Entry")
					.asSubclass(GuiEventListener.class);
			registry.register(type, LanguageSelectionBehaviour::new);
		} catch (ClassNotFoundException exception) {
			throw new IllegalStateException("Language selection entry class is unavailable", exception);
		}
	}

	private static void adjustSlider(AbstractSliderButton slider, int direction) {
		var accessor = (SliderAccessor) slider;
		boolean editing = accessor.controlify$canChangeValue();
		try {
			// Secondary navigation adjusts without entering the widget's editing mode.
			accessor.controlify$setCanChangeValue(true);
			int key = direction < 0 ? InputConstants.KEY_LEFT : InputConstants.KEY_RIGHT;
			slider.keyPressed(new KeyEvent(key, 0, 0));
		} finally {
			accessor.controlify$setCanChangeValue(editing && slider.isFocused());
		}
	}

	private static ScreenBehaviour createCursorAndFocusBehaviours(ScreenContext<?> screen) {
		var context = (ScreenContextImpl<?>) screen;
		var cursor = ScreenBehaviours.chain(
				new CursorMovementBehaviour(context),
				ScreenBehaviours.conditional(
						() -> context.virtualMouse().mode().scrolls(),
						new CursorScrollBehaviour(context)
				),
				new CursorRenderBehaviour(context)
		);

		return ScreenBehaviours.chain(
				new CursorModeBehaviour(context),
				ScreenBehaviours.conditional(context::isVirtualMouseEnabled, cursor),
				new FocusManagementBehaviour(context),
				ScreenBehaviours.conditional(
						() -> !context.isVirtualMouseEnabled(),
						new FocusedElementBehaviour(context)
				)
		);
	}

	private static ScreenBehaviour createDefaultControls(ScreenContext<?> screen) {
		var context = (ScreenContextImpl<?>) screen;
		return ScreenBehaviours.chain(
				ScreenBehaviours.conditional(
						() -> !context.isVirtualMouseEnabled(),
						new FocusInputBehaviour(context)
				),
				new TabNavigationBehaviour(context),
				ScreenBehaviours.conditional(
						() -> context.isVirtualMouseEnabled() && context.virtualMouse().mode().emulatesClicks(),
						new CursorClickBehaviour(context)
				),
				new BackButtonBehaviour(context),
				new ScreenSnapPointBehaviour(context)
		);
	}
}
