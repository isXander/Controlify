/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.screen;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.VirtualMouseMode;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.action.radial.RadialIcon;
import dev.isxander.controlify.impl.action.radial.RadialIconExtractor;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.haptic.HapticEffects;
import dev.isxander.controlify.impl.gui.screen.RadialItems.RadialPage;
import dev.isxander.controlify.impl.sound.ControlifyClientSounds;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.impl.utils.animation.api.Animation;
import dev.isxander.controlify.impl.utils.animation.api.EasingFunction;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RadialMenuScreen extends Screen {
	private final ControllerImpl controller;
	private final @Nullable Screen parent;
	private final Component footerText;
	private final List<RadialItem[]> userPages;
	private final List<RadialPage> bonusPages;
	private final @Nullable ActionAccessor openAction;

	private RadialButton[] buttons = new RadialButton[0];
	private float radialRadius;
	private int pageIndex;
	private int selectedButton = -1;
	private int idleTicks;
	private final int idleTicksTimeout;

	/**
	 * Creates a single-page radial menu. Existing special-purpose radial menus use this path.
	 */
	public RadialMenuScreen(ControllerImpl controller, ActionAccessor openAction, RadialItem[] items, Component text, @Nullable Screen parent) {
		this(controller, openAction, Collections.singletonList(items), List.of(), text, parent);
	}

	/**
	 * Creates a paged radial menu. Page boundaries are a presentation concern and are not persisted.
	 */
	public RadialMenuScreen(ControllerImpl controller, @Nullable ActionAccessor openAction, List<RadialItem[]> pages, Component text, @Nullable Screen parent) {
		this(controller, openAction, pages, List.of(), text, parent);
	}

	public RadialMenuScreen(
			ControllerImpl controller,
			@Nullable ActionAccessor openAction,
			List<RadialItem[]> userPages,
			List<RadialPage> bonusPages,
			Component text,
			@Nullable Screen parent
	) {
		super(text);
		this.footerText = text;
		this.controller = controller;
		this.userPages = userPages.isEmpty()
				? Collections.singletonList(new RadialItem[0])
				: userPages.stream().map(RadialItem[]::clone).toList();
		this.bonusPages = bonusPages.stream()
				.map(page -> new RadialPage(page.name(), page.items().clone()))
				.toList();
		this.pageIndex = this.bonusPages.size();
		this.parent = parent;
		this.idleTicksTimeout = controller.input().settings().radialMenu.radialButtonFocusTimeoutTicks;
		this.openAction = openAction;
	}

	@Override
	protected void init() {
		buildCurrentPage();
	}

	private void buildCurrentPage() {
		Arrays.stream(buttons).forEach(this::removeWidget);
		selectedButton = -1;
		idleTicks = 0;
		setFocused(null);

		RadialItem[] items = getCurrentPageItems();
		buttons = new RadialButton[items.length];
		if (items.length == 0) {
			return;
		}

		int centerX = this.width / 2;
		int centerY = this.height / 2;
		float buttonDiameter = (float) Math.sqrt(32 * 32 + 32 * 32) + 8;
		float circumference = buttonDiameter * items.length;
		radialRadius = Math.max(circumference / Mth.TWO_PI, 43);

		Animation animation = Animation.of(5).easing(EasingFunction.EASE_OUT_QUAD);
		for (int i = 0; i < items.length; i++) {
			float angle = Mth.TWO_PI * i / items.length - (90 * Mth.DEG_TO_RAD);
			float x = centerX + Mth.cos(angle) * radialRadius;
			float y = centerY + Mth.sin(angle) * radialRadius;

			RadialButton button = buttons[i] = new RadialButton(items[i], centerX - 16, centerY - 16);
			animation
					.consumerF(button::setX, centerX - 16, x - 16)
					.consumerF(button::setY, centerY - 16, y - 16);
			addRenderableWidget(button);
		}
		animation.play();
	}

	private void changePage(int direction) {
		if (pageCount() <= 1) {
			return;
		}

		pageIndex = Math.floorMod(pageIndex + direction, pageCount());
		buildCurrentPage();
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ControlifyClientSounds.SCREEN_FOCUS_CHANGE.get(), 1f));
		controller.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));
	}

	private void onControllerInput(@NonNull ControllerImpl controller) {
		if (this.controller != controller) {
			return;
		}

		if (openAction == null) {
			if (ControlifyActions.GUI_BACK.on(controller).pulse().consumeAllPulses()) {
				onClose();
			}
			return;
		}

		if (!openAction.on(controller).latch().isLatchActive()) {
			if (selectedButton >= 0 && selectedButton < buttons.length && buttons[selectedButton].invoke()) {
				playClickSound();
			}
			onClose();
			return;
		}

		if (buttons.length == 0) {

			return;

		}

		float x = ControlifyActions.RADIAL_AXIS_RIGHT.on(controller).continuous().getContinuous()
				- ControlifyActions.RADIAL_AXIS_LEFT.on(controller).continuous().getContinuous();
		float y = ControlifyActions.RADIAL_AXIS_DOWN.on(controller).continuous().getContinuous()
				- ControlifyActions.RADIAL_AXIS_UP.on(controller).continuous().getContinuous();
		float threshold = controller.input().settings().buttonActivationThreshold;

		if (Math.abs(x) >= threshold || Math.abs(y) >= threshold) {
			float angle = Mth.wrapDegrees(Mth.RAD_TO_DEG * (float) Mth.atan2(y, x) - 90f) + 180f;
			float each = 360f / buttons.length;
			int newSelected = Mth.floor((angle + each / 2f) / each) % buttons.length;

			if (newSelected != selectedButton) {
				selectedButton = newSelected;
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ControlifyClientSounds.SCREEN_FOCUS_CHANGE.get(), 1f));
				controller.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));
			}

			for (int i = 0; i < buttons.length; i++) {
				boolean selected = i == selectedButton;
				buttons[i].setFocused(selected);
				if (selected) {
					setFocused(buttons[i]);
				}
			}
			idleTicks = 0;
		} else {
			idleTicks++;
			if (idleTicks >= idleTicksTimeout && selectedButton != -1) {
				selectedButton = -1;
				for (RadialButton button : buttons) button.setFocused(false);
				controller.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));
			}
		}
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		if (buttons.length == 0) {
			graphics.centeredText(font, Component.translatable("controlify.radial_menu.empty"), width / 2, height / 2, -1);
		}
		graphics.centeredText(font, footerText, width / 2, height - 39, -1);

		if (pageCount() > 1) {
			Component page = getPageTitle();
			graphics.centeredText(font, page, width / 2, height - 25, -1);
			renderPageDots(graphics);

			Component previous = ActionGlyphs.action(ControlifyActions.GUI_PREV_TAB.actionId());
			Component next = ActionGlyphs.action(ControlifyActions.GUI_NEXT_TAB.actionId());
			int pageWidth = font.width(page);
			graphics.text(font, previous, width / 2 - pageWidth / 2 - font.width(previous) - 8, height - 25, -1);
			graphics.text(font, next, width / 2 + pageWidth / 2 + 8, height - 25, -1);
		}
	}

	private RadialItem[] getCurrentPageItems() {
		if (pageIndex < bonusPages.size()) {
			return bonusPages.get(pageIndex).items();
		}

		return userPages.get(pageIndex - bonusPages.size());
	}

	private Component getPageTitle() {
		if (pageIndex < bonusPages.size()) {
			return bonusPages.get(pageIndex).name();
		}

		return Component.translatable(
				"controlify.radial_menu.page",
				pageIndex + 1 - bonusPages.size(),
				userPages.size()
		);
	}

	private int pageCount() {
		return bonusPages.size() + userPages.size();
	}

	private void renderPageDots(GuiGraphicsExtractor graphics) {
		int visibleCount = Math.min(pageCount(), 15);
		int firstPage = Math.clamp(pageIndex - visibleCount / 2, 0, pageCount() - visibleCount);
		int totalWidth = visibleCount * 5 - 2;
		int startX = (width - totalWidth) / 2;

		for (int index = 0; index < visibleCount; index++) {
			int representedPage = firstPage + index;
			int color = representedPage == pageIndex ? 0xffffffff : 0x80ffffff;
			graphics.fill(startX + index * 5, height - 13, startX + index * 5 + 3, height - 10, color);
		}
	}

	@Override
	public void onClose() {
		MinecraftUtil.setScreen(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void playClickSound() {
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
	}

	public interface RadialItem {
		Component name();

		RadialIcon icon();

		boolean playAction();
	}

	public class RadialButton implements Renderable, GuiEventListener, NarratableEntry {
		public static final Identifier TEXTURE = CUtil.rl("textures/gui/radial-buttons.png");

		private int x, y;
		private float translateX, translateY;
		private boolean focused;
		private final RadialItem item;
		private final MultiLineLabel name;

		private RadialButton(RadialItem item, float x, float y) {
			setX(x);
			setY(y);
			this.item = item;
			this.name = MultiLineLabel.create(font, item.name(), (int) (radialRadius * 2 - 32));
		}

		@Override
		public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			var pose = graphics.pose().pushMatrix();
			pose.translate(x + translateX, y + translateY);
			pose.pushMatrix();
			pose.scale(2, 2);
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, focused ? 16 : 0, 0, 16, 16, 32, 16);
			pose.popMatrix();

			pose.pushMatrix();
			pose.translate(4, 4);
			pose.scale(1.5f, 1.5f);
			RadialIconExtractor.extract(graphics, item.icon(), 0, 0);
			pose.popMatrix();
			pose.popMatrix();

			if (focused) {
				int topY = height / 2 - font.lineHeight / 2 - ((name.getLineCount() - 1) * font.lineHeight);
				name.visitLines(TextAlignment.CENTER, width / 2, topY, font.lineHeight, graphics.textRenderer());
			}
		}

		private boolean invoke() {
			return item.playAction();
		}

		private void setX(float x) {
			this.x = (int) x;
			this.translateX = x - this.x;
		}

		private void setY(float y) {
			this.y = (int) y;
			this.translateY = y - this.y;
		}

		@Override
		public boolean isFocused() {
			return focused;
		}

		@Override
		public void setFocused(boolean focused) {
			this.focused = focused;
		}

		@Override
		public @NonNull NarrationPriority narrationPriority() {
			return focused ? NarrationPriority.FOCUSED : NarrationPriority.NONE;
		}

		@Override
		public void updateNarration(NarrationElementOutput builder) {
			builder.add(NarratedElementType.TITLE, item.name());
		}

		@Override
		public @NonNull ScreenRectangle getRectangle() {
			return new ScreenRectangle(x, y, 32, 32);
		}
	}

	public static final class Behaviour implements ScreenBehaviour {
		private final RadialMenuScreen screen;
		private final ScreenContext<? extends RadialMenuScreen> context;

		public Behaviour(ScreenContext<? extends RadialMenuScreen> context) {
			this.context = context;
			this.screen = context.screen();
			context.virtualMouse().setMode(VirtualMouseMode.DISABLED);
		}

		@Override
		public void handleNavigation(Controller input, Runnable next) {
			var controller = (ControllerImpl) input;
			this.screen.onControllerInput(controller);
			if (MinecraftUtil.getScreen() != this.context.screen()) {
				return;
			}
			while (ControlifyActions.GUI_NEXT_TAB.on(controller).pulse().consumePulse()) {
				this.screen.changePage(1);
			}
			while (ControlifyActions.GUI_PREV_TAB.on(controller).pulse().consumePulse()) {
				this.screen.changePage(-1);
			}
			next.run();
		}
	}
}
