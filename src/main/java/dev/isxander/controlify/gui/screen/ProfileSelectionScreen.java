/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileSelectionScreen extends Screen implements ScreenControllerEventListener {
	private static final int CARD_WIDTH = 128;
	private static final int CARD_HEIGHT = 104;
	private static final int CARD_SPACING = 6;
	private static final int ICON_SIZE = 44;

	private final Screen parent;
	private final List<ProfileCard> profileCards = new ArrayList<>();

	private HeaderAndFooterLayout layout;
	private ContainerEventHandler profileGridContainer;
	private Button rememberButton;
	private Button onceButton;
	private Button deleteButton;
	private int selectedProfileIndex;
	private int initialFocusIndex;

	public ProfileSelectionScreen(Screen parent) {
		super(Component.translatable("controlify.gui.profile_selection.title"));
		this.parent = parent;
		this.selectedProfileIndex = Controlify.instance().config().getActiveProfileIndex();
		this.initialFocusIndex = this.selectedProfileIndex;
	}

	@Override
	protected void init() {
		this.profileCards.clear();
		this.profileGridContainer = null;
		this.layout = new HeaderAndFooterLayout(this);
		this.layout.addTitleHeader(this.title, this.font);

		int availableWidth = Math.max(CARD_WIDTH, this.width - 8);
		int columns = Math.max(1, (availableWidth + CARD_SPACING) / (CARD_WIDTH + CARD_SPACING));
		GridLayout profileGrid = new GridLayout().spacing(CARD_SPACING);
		GridLayout.RowHelper row = profileGrid.createRowHelper(columns);

		Controlify.instance().config().getSettings().profileSettings().forEach((index, profile) -> {
			ProfileCard card = new ProfileCard(index, profile);
			this.profileCards.add(card);
			row.addChild(card);
		});
		row.addChild(new CreateProfileCard());
		profileGrid.arrangeElements();
		GridLayout scrollContent = new GridLayout();
		scrollContent.addChild(profileGrid, 0, 0, settings -> settings.paddingTop(10));
		scrollContent.arrangeElements();

		//? if >=26.2 {
		ScrollableLayout scrollable = new ScrollableLayout(
				this.minecraft,
				scrollContent,
				this.layout.getContentHeight(),
				ScrollableLayout.ReserveStrategy.RIGHT
		);
		scrollable.setScrollbarSpacing(0);
		//?} else {
		/*ScrollableLayout scrollable = new ScrollableLayout(this.minecraft, scrollContent, this.layout.getContentHeight());
		*///?}
		scrollable.setMinWidth(columns * CARD_WIDTH + Math.max(0, columns - 1) * CARD_SPACING);
		scrollable.setMaxHeight(this.layout.getContentHeight());
		scrollable.setMinHeight(this.layout.getContentHeight());
		scrollable.visitWidgets(widget -> {
			if (widget instanceof ContainerEventHandler container) {
				this.profileGridContainer = container;
			}
		});
		this.layout.addToContents(scrollable, LayoutSettings::alignHorizontallyCenter);

		LinearLayout footer = LinearLayout.horizontal().spacing(6);
		this.rememberButton = footer.addChild(
				Button.builder(
								Component.translatable("controlify.gui.profile_selection.remember"),
								button -> switchProfile(this.selectedProfileIndex, true)
						)
						.width(90)
						.tooltip(Tooltip.create(Component.translatable("controlify.gui.profile_selection.remember.tooltip")))
						.build()
		);
		this.onceButton = footer.addChild(
				Button.builder(
								Component.translatable("controlify.gui.profile_selection.once"),
								button -> switchProfile(this.selectedProfileIndex, false)
						)
						.width(90)
						.tooltip(Tooltip.create(Component.translatable("controlify.gui.profile_selection.once.tooltip")))
						.build()
		);
		this.deleteButton = footer.addChild(
				Button.builder(
								Component.translatable("controlify.gui.profile_selection.delete"),
								button -> confirmDeleteProfile()
						)
						.width(90)
						.tooltip(Tooltip.create(Component.translatable("controlify.gui.profile_selection.delete.tooltip")))
						.build()
		);
		Button backButton = footer.addChild(
				Button.builder(CommonComponents.GUI_BACK, button -> this.onClose())
						.width(90)
						.build()
		);
		this.layout.addToFooter(footer);

		ButtonGuideApi.addGuideToButton(this.rememberButton, ControlifyBindings.GUI_PRESS, ButtonGuidePredicate.always());
		ButtonGuideApi.addGuideToButton(this.onceButton, ControlifyBindings.GUI_ABSTRACT_ACTION_1, ButtonGuidePredicate.always());
		ButtonGuideApi.addGuideToButton(this.deleteButton, ControlifyBindings.GUI_ABSTRACT_ACTION_2, ButtonGuidePredicate.always());
		ButtonGuideApi.addGuideToButton(backButton, ControlifyBindings.GUI_BACK, ButtonGuidePredicate.always());

		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
		this.updateActionButtons();
	}

	@Override
	protected void setInitialFocus() {
		ProfileCard target = this.profileCards.stream()
				.filter(card -> card.profileIndex == this.initialFocusIndex)
				.findFirst()
				.orElse(null);
		if (target != null && this.profileGridContainer != null) {
			this.setFocused(this.profileGridContainer);
			this.profileGridContainer.setFocused(target);
		} else {
			super.setInitialFocus();
		}
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		Identifier headerTexture = this.minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				headerTexture,
				0, this.layout.getHeaderHeight(),
				0.0F, 0.0F,
				this.width, 2,
				32, 2
		);
		Identifier footerTexture = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				footerTexture,
				0, this.height - this.layout.getFooterHeight(),
				0.0F, 0.0F,
				this.width, 2,
				32, 2
		);
	}

	@Override
	public void onControllerInput(ControllerEntity controller) {
		if (this.selectedProfileIndex >= 0
				&& ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
			this.switchProfile(this.selectedProfileIndex, false);
		} else if (this.deleteButton.active
				&& ControlifyBindings.GUI_ABSTRACT_ACTION_2.on(controller).justPressed()) {
			this.confirmDeleteProfile();
		}
	}

	@Override
	public void onClose() {
		MinecraftUtil.setScreen(this.parent);
	}

	private void selectProfile(int profileIndex) {
		if (this.selectedProfileIndex != profileIndex) {
			this.selectedProfileIndex = profileIndex;
			this.updateActionButtons();
		}
	}

	private void selectCreateProfile() {
		if (this.selectedProfileIndex != -1) {
			this.selectedProfileIndex = -1;
			this.updateActionButtons();
		}
	}

	private void updateActionButtons() {
		boolean hasProfile = this.selectedProfileIndex >= 0;
		if (this.rememberButton != null) {
			this.rememberButton.active = hasProfile;
		}
		if (this.onceButton != null) {
			this.onceButton.active = hasProfile;
		}
		if (this.deleteButton != null) {
			this.deleteButton.active = hasProfile
					&& this.selectedProfileIndex != Controlify.instance().config().getActiveProfileIndex();
		}
	}

	private void switchProfile(int profileIndex, boolean remember) {
		try {
			if (!Controlify.instance().switchProfile(profileIndex, remember)) {
				showFailureToast("controlify.toast.profile_switch.locked");
				return;
			}
			MinecraftUtil.setScreen(this.parent);
		} catch (IOException e) {
			CUtil.LOGGER.error("Failed to switch Controlify profile", e);
			showFailureToast("controlify.toast.profile_switch.failed");
		}
	}

	private void createProfile() {
		try {
			int profileIndex = Controlify.instance().config().createProfile();
			this.selectedProfileIndex = profileIndex;
			this.initialFocusIndex = profileIndex;
			this.rebuildWidgets();
		} catch (IOException | ArithmeticException e) {
			CUtil.LOGGER.error("Failed to create Controlify profile", e);
			showFailureToast("controlify.toast.profile_create.failed");
		}
	}

	private void confirmDeleteProfile() {
		if (this.selectedProfileIndex < 0
				|| this.selectedProfileIndex == Controlify.instance().config().getActiveProfileIndex()) {
			return;
		}

		int profileIndex = this.selectedProfileIndex;
		ProfileSettings profile = Controlify.instance().config().getSettings().getProfileSettings(profileIndex);
		if (profile == null) {
			return;
		}

		Component name = profileName(profileIndex, profile);
		MinecraftUtil.setScreen(new ConfirmScreen(
				confirmed -> {
					MinecraftUtil.setScreen(this);
					if (confirmed) {
						deleteProfile(profileIndex);
					}
				},
				Component.translatable("controlify.gui.profile_selection.delete.confirm.title"),
				Component.translatable("controlify.gui.profile_selection.delete.confirm.message", name),
				Component.translatable("controlify.gui.profile_selection.delete"),
				CommonComponents.GUI_CANCEL
		));
	}

	private void deleteProfile(int profileIndex) {
		try {
			if (!Controlify.instance().config().deleteProfile(profileIndex)) {
				showFailureToast("controlify.toast.profile_delete.locked");
				return;
			}
			this.selectedProfileIndex = Controlify.instance().config().getActiveProfileIndex();
			this.initialFocusIndex = this.selectedProfileIndex;
			this.rebuildWidgets();
		} catch (IOException e) {
			CUtil.LOGGER.error("Failed to delete Controlify profile", e);
			showFailureToast("controlify.toast.profile_delete.failed");
		}
	}

	private void showFailureToast(String messageKey) {
		MinecraftUtil.sendToast(
				Component.translatable("controlify.toast.profile.title"),
				Component.translatable(messageKey),
				false
		);
	}

	private ControllerDisplay controllerDisplay(ProfileSettings profile) {
		if (profile.controllerUid == null) {
			return new ControllerDisplay(
					Component.translatable("controlify.gui.profile_selection.automatic"),
					ControllerType.DEFAULT.getIconSprite()
			);
		}

		ControllerEntity connected = Controlify.instance().getControllerManager()
				.flatMap(manager -> manager.getConnectedControllers().stream()
						.filter(controller -> profile.controllerUid.equals(controller.uid()))
						.findFirst())
				.orElse(null);
		if (connected != null) {
			return new ControllerDisplay(
					Component.literal(connected.name()),
					connected.info().type().getIconSprite()
			);
		}

		DeviceSettings device = Controlify.instance().config().getSettings().deviceSettings().get(profile.controllerUid);
		if (device != null) {
			return new ControllerDisplay(
					Component.literal(device.name),
					device.controllerType.withPrefix("controllers/")
			);
		}

		return new ControllerDisplay(
				Component.literal(profile.controllerUid),
				ControllerType.DEFAULT.getIconSprite()
		);
	}

	private Component profileName(int profileIndex, ProfileSettings profile) {
		int displayedIndex = profileIndex + 1;
		return profile.name == null
				? Component.translatable("controlify.gui.carousel.entry.profile", displayedIndex)
				: Component.translatable("controlify.gui.carousel.entry.named_profile", displayedIndex, profile.name);
	}

	private Component trim(Component text, int maxWidth) {
		String value = text.getString();
		if (this.font.width(value) <= maxWidth) {
			return text;
		}
		return Component.literal(this.font.plainSubstrByWidth(value, Math.max(0, maxWidth - this.font.width("..."))) + "...");
	}

	private void extractCardBackground(GuiGraphicsExtractor graphics, AbstractButton card, boolean activeProfile) {
		boolean focused = card.isFocused() && this.getFocused() == this.profileGridContainer;
		int background = focused
				? 0xDD303030
				: card.isHovered() ? 0xCC252525 : 0xAA101010;
		int border = focused
				? 0xFFFFFFFF
				: card.isHovered() ? 0xFFAAAAAA : 0xFF555555;

		graphics.fill(
				card.getX() + 1, card.getY() + 1,
				card.getRight() - 1, card.getBottom() - 1,
				background
		);
		graphics.outline(card.getX(), card.getY(), card.getWidth(), card.getHeight(), border);
		if (activeProfile) {
			graphics.fill(
					card.getX() + 2, card.getY() + 2,
					card.getX() + 5, card.getBottom() - 2,
					0xFF55FF55
			);
		}
	}

	private class ProfileCard extends AbstractButton {
		private final int profileIndex;
		private final Component profileName;
		private final ControllerDisplay controller;

		private ProfileCard(int profileIndex, ProfileSettings profile) {
			super(0, 0, CARD_WIDTH, CARD_HEIGHT, profileName(profileIndex, profile));
			this.profileIndex = profileIndex;
			this.profileName = getMessage();
			this.controller = controllerDisplay(profile);
		}

		@Override
		public void onPress(InputWithModifiers input) {
			switchProfile(this.profileIndex, true);
		}

		@Override
		public void onClick(MouseButtonEvent event, boolean doubleClick) {
			selectProfile(this.profileIndex);
		}

		@Override
		public void setFocused(boolean focused) {
			super.setFocused(focused);
			if (focused) {
				selectProfile(this.profileIndex);
			}
		}

		@Override
		protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			boolean activeProfile = Controlify.instance().config().getActiveProfileIndex() == this.profileIndex;
			extractCardBackground(graphics, this, activeProfile);
			int centerX = this.getX() + this.getWidth() / 2;
			int iconX = centerX - ICON_SIZE / 2;
			int iconY = this.getY() + 7;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.controller.icon(), iconX, iconY, ICON_SIZE, ICON_SIZE);
			graphics.centeredText(font, trim(this.profileName, this.getWidth() - 10), centerX, this.getY() + 56, 0xFFFFFFFF);
			graphics.centeredText(font, trim(this.controller.name(), this.getWidth() - 10), centerX, this.getY() + 70, 0xFFA0A0A0);

			if (activeProfile) {
				graphics.centeredText(
						font,
						Component.translatable("controlify.gui.profile_selection.active").withStyle(ChatFormatting.GREEN),
						centerX,
						this.getY() + 86,
						0xFF55FF55
				);
			}
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
			this.defaultButtonNarrationText(output);
		}
	}

	private class CreateProfileCard extends AbstractButton {
		private CreateProfileCard() {
			super(
					0, 0,
					CARD_WIDTH, CARD_HEIGHT,
					Component.translatable("controlify.gui.profile_selection.create")
			);
		}

		@Override
		public void onPress(InputWithModifiers input) {
			createProfile();
		}

		@Override
		public void setFocused(boolean focused) {
			super.setFocused(focused);
			if (focused) {
				selectCreateProfile();
			}
		}

		@Override
		protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			extractCardBackground(graphics, this, false);
			int centerX = this.getX() + this.getWidth() / 2;
			int plusY = this.getY() + 31;
			graphics.fill(centerX - 2, plusY - 14, centerX + 3, plusY + 15, 0xFFFFFFFF);
			graphics.fill(centerX - 14, plusY - 2, centerX + 15, plusY + 3, 0xFFFFFFFF);
			graphics.centeredText(font, getMessage(), centerX, this.getY() + 70, 0xFFFFFFFF);
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
			this.defaultButtonNarrationText(output);
		}
	}

	private record ControllerDisplay(Component name, Identifier icon) {
	}
}
