/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.bindings.RadialIconExtractor;
import dev.isxander.controlify.bindings.RadialIconManager;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.haptic.HapticEffects;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.utils.MinecraftUtil;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RadialMenuEditScreen extends Screen implements ScreenProcessorProvider {
	private static final int COLUMN_GAP = 8;
	private static final int COLUMN_PADDING = 4;
	private static final int COLUMN_HEADER_HEIGHT = 24;
	private static final int SECTION_HEIGHT = 19;
	private static final int ACTION_HEIGHT = 26;
	private static final int FOOTER_BUTTON_WIDTH = 200;
	private static final int ROW_GAP = 2;

	private final Screen parent;
	private final ControllerEntity controller;
	private final RadialMenuEditModel model;
	private final List<InputBinding> candidates;
	private final Map<Identifier, InputBinding> candidatesById;
	private final Processor processor = new Processor(this);

	private HeaderAndFooterLayout layout;
	private ContainerEventHandler availableContainer;
	private ContainerEventHandler equippedContainer;
	private final Map<Identifier, ActionEntry> availableEntries = new LinkedHashMap<>();
	private final Map<Identifier, ActionEntry> equippedEntries = new LinkedHashMap<>();
	private Identifier focusAfterRebuild;
	private int equippedInsertionIndex;

	public RadialMenuEditScreen(Screen parent, ControllerEntity controller) {
		super(Component.translatable("controlify.radial_menu.editor.title"));
		this.parent = parent;
		this.controller = controller;
		this.model = new RadialMenuEditModel(controller.settings().input.radialMenu.radialActions);
		this.candidates = controller.input().orElseThrow().getAllBindings().stream()
				.filter(RadialIconManager.INSTANCE::isRadialCandidate)
				.toList();
		this.candidatesById = candidates.stream().collect(Collectors.toMap(
				InputBinding::id,
				Function.identity(),
				(first, ignored) -> first,
				LinkedHashMap::new
		));
		this.equippedInsertionIndex = model.actions().size();
	}

	@Override
	protected void init() {
		availableEntries.clear();
		equippedEntries.clear();
		availableContainer = null;
		equippedContainer = null;

		layout = new HeaderAndFooterLayout(this);
		layout.addTitleHeader(title, font);

		int columnWidth = Math.max(100, (width - COLUMN_GAP - 16) / 2);
		GridLayout columns = new GridLayout().columnSpacing(COLUMN_GAP);
		GridLayout.RowHelper row = columns.createRowHelper(2);
		row.addChild(createAvailableColumn(columnWidth));
		row.addChild(createEquippedColumn(columnWidth));
		columns.arrangeElements();
		layout.addToContents(columns, LayoutSettings::alignHorizontallyCenter);

		LinearLayout footer = LinearLayout.horizontal();
		Button done = footer.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> commitAndClose())
				.width(FOOTER_BUTTON_WIDTH)
				.build());
		ButtonGuideApi.addGuideToButton(done, ControlifyBindings.GUI_BACK, ButtonGuidePredicate.always());
		layout.addToFooter(footer);

		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}

	private LinearLayout createAvailableColumn(int width) {
		LinearLayout column = LinearLayout.vertical().spacing(4);
		long availableCount = candidates.stream().filter(binding -> !model.actions().contains(binding.id())).count();
		column.addChild(new ColumnHeader(
				width,
				Component.translatable("controlify.radial_menu.editor.available"),
				availableCount
		));

		int entryWidth = width - COLUMN_PADDING * 2;
		LinearLayout content = LinearLayout.vertical().spacing(ROW_GAP);
		if (model.carriedIsEquipped()) {
			content.addChild(new TransferTarget(
					entryWidth,
					Component.translatable("controlify.radial_menu.editor.remove"),
					false,
					this::finishUnequip
			));
		}

		Map<Component, List<InputBinding>> grouped = candidates.stream()
				.filter(binding -> !model.actions().contains(binding.id()))
				.collect(Collectors.groupingBy(InputBinding::category, LinkedHashMap::new, Collectors.toList()));
		grouped.forEach((category, bindings) -> {
			content.addChild(new SectionDivider(entryWidth, category, false));
			for (InputBinding binding : bindings) {
				ActionEntry entry = new ActionEntry(binding.id(), binding.name(), false, -1, entryWidth);
				availableEntries.put(binding.id(), entry);
				content.addChild(entry);
			}
		});
		if (grouped.isEmpty()) {
			content.addChild(new EmptyState(
					entryWidth,
					Component.translatable("controlify.radial_menu.editor.none_available")
			));
		}

		ScrollableLayout scrollable = createScrollable(padded(content), width);
		scrollable.visitWidgets(widget -> {
			if (widget instanceof ContainerEventHandler container) availableContainer = container;
		});
		column.addChild(scrollable);
		return column;
	}

	private LinearLayout createEquippedColumn(int width) {
		LinearLayout column = LinearLayout.vertical().spacing(4);
		column.addChild(new ColumnHeader(
				width,
				Component.translatable("controlify.radial_menu.editor.equipped"),
				model.actions().size()
		));

		int entryWidth = width - COLUMN_PADDING * 2;
		LinearLayout content = LinearLayout.vertical().spacing(ROW_GAP);

		List<Identifier> actions = model.actions();
		for (int index = 0; index < actions.size(); index++) {
			if (index % 8 == 0) {
				content.addChild(new SectionDivider(
						entryWidth,
						Component.translatable("controlify.radial_menu.editor.page", index / 8 + 1),
						true
				));
			}

			Identifier id = actions.get(index);
			InputBinding binding = candidatesById.get(id);
			Component name = binding == null
					? Component.translatable("controlify.radial_menu.editor.missing", id.toString()).withStyle(ChatFormatting.RED)
					: binding.name();
			ActionEntry entry = new ActionEntry(id, name, true, index, entryWidth);
			equippedEntries.put(id, entry);
			content.addChild(entry);
		}

		if (model.isCarrying()) {
			content.addChild(new TransferTarget(
					entryWidth,
					Component.translatable(
							model.carriedIsEquipped()
									? "controlify.radial_menu.editor.move_to_end"
									: "controlify.radial_menu.editor.add_to_end"
					),
					true,
					this::finishAtEnd
			));
		} else if (actions.isEmpty()) {
			content.addChild(new EmptyState(entryWidth, Component.translatable("controlify.radial_menu.editor.none")));
		}

		ScrollableLayout scrollable = createScrollable(padded(content), width);
		scrollable.visitWidgets(widget -> {
			if (widget instanceof ContainerEventHandler container) equippedContainer = container;
		});
		column.addChild(scrollable);
		return column;
	}

	private GridLayout padded(LinearLayout content) {
		GridLayout padded = new GridLayout();
		padded.addChild(content, 0, 0, settings -> settings.paddingHorizontal(COLUMN_PADDING).paddingVertical(4));
		padded.arrangeElements();
		return padded;
	}

	private ScrollableLayout createScrollable(Layout content, int width) {
		int height = layout.getContentHeight() - COLUMN_HEADER_HEIGHT - 4;
		//? if >=26.2 {
		ScrollableLayout scrollable = new ScrollableLayout(
				minecraft,
				content,
				height,
				ScrollableLayout.ReserveStrategy.RIGHT
		);
		scrollable.setScrollbarSpacing(0);
		//?} else {
		/*ScrollableLayout scrollable = new ScrollableLayout(minecraft, content, height);
		*///?}
		scrollable.setMinWidth(width);
		scrollable.setMaxHeight(height);
		scrollable.setMinHeight(height);
		return scrollable;
	}

	private void activate(Identifier id, boolean equipped) {
		if (!model.isCarrying()) {
			model.pickUp(id);
			if (equipped) equippedInsertionIndex = model.actions().indexOf(id);
			focusAfterRebuild = id;
			rebuildWidgets();
		} else if (Objects.equals(id, model.carried())) {
			model.drop();
			focusAfterRebuild = id;
			rebuildWidgets();
		} else if (equipped) {
			int destination = model.actions().indexOf(id);
			if (model.carriedIsEquipped()) {
				model.moveCarriedTo(destination);
			} else {
				model.equipCarried(destination);
			}
			focusAfterRebuild = model.carried();
			model.drop();
			rebuildWidgets();
		} else if (model.carriedIsEquipped()) {
			model.unequipCarried();
			focusAfterRebuild = model.carried();
			model.drop();
			rebuildWidgets();
		}
	}

	private boolean moveCarried(ScreenDirection direction) {
		Identifier carried = model.carried();
		if (carried == null) return false;

		boolean changed = switch (direction) {
			case LEFT -> {
				if (model.carriedIsEquipped()) {
					equippedInsertionIndex = model.unequipCarried();
					yield true;
				}
				yield false;
			}
			case RIGHT -> model.equipCarried(equippedInsertionIndex);
			case UP -> model.moveCarried(-1);
			case DOWN -> model.moveCarried(1);
		};

		if (changed) {
			if (model.carriedIsEquipped()) equippedInsertionIndex = model.actions().indexOf(carried);
			focusAfterRebuild = carried;
			rebuildWidgets();
		}
		return changed;
	}

	private void dropCarried() {
		Identifier carried = model.carried();
		model.drop();
		focusAfterRebuild = carried;
		rebuildWidgets();
	}

	private void finishUnequip() {
		Identifier carried = model.carried();
		model.unequipCarried();
		model.drop();
		focusAfterRebuild = carried;
		rebuildWidgets();
	}

	private void finishAtEnd() {
		Identifier carried = model.carried();
		if (model.carriedIsEquipped()) {
			model.moveCarriedTo(model.actions().size() - 1);
		} else {
			model.equipCarried(model.actions().size());
		}
		model.drop();
		focusAfterRebuild = carried;
		rebuildWidgets();
	}

	@Override
	protected void setInitialFocus() {
		ActionEntry target = focusAfterRebuild == null ? null : equippedEntries.get(focusAfterRebuild);
		ContainerEventHandler container = equippedContainer;
		if (target == null && focusAfterRebuild != null) {
			target = availableEntries.get(focusAfterRebuild);
			container = availableContainer;
		}
		focusAfterRebuild = null;

		if (target != null && container != null) {
			setFocused(container);
			container.setFocused(target);
		} else {
			super.setInitialFocus();
		}
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		Identifier headerTexture = minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				headerTexture,
				0, layout.getHeaderHeight(),
				0.0F, 0.0F,
				width, 2,
				32, 2
		);
		Identifier footerTexture = minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				footerTexture,
				0, height - layout.getFooterHeight(),
				0.0F, 0.0F,
				width, 2,
				32, 2
		);
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		if (model.isCarrying()) {
			if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_SPACE) {
				dropCarried();
				return true;
			}
			if (event.key() == GLFW.GLFW_KEY_LEFT) {
				moveCarried(ScreenDirection.LEFT);
				return true;
			}
			if (event.key() == GLFW.GLFW_KEY_RIGHT) {
				moveCarried(ScreenDirection.RIGHT);
				return true;
			}
			if (event.key() == GLFW.GLFW_KEY_UP) {
				moveCarried(ScreenDirection.UP);
				return true;
			}
			if (event.key() == GLFW.GLFW_KEY_DOWN) {
				moveCarried(ScreenDirection.DOWN);
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		commitAndClose();
	}

	private void commitAndClose() {
		List<Identifier> configured = controller.settings().input.radialMenu.radialActions;
		if (!configured.equals(model.actions())) {
			configured.clear();
			configured.addAll(model.actions());
			Controlify.instance().config().markDirty();
			Controlify.instance().config().saveIfDirty();
		}
		MinecraftUtil.setScreen(parent);
	}

	@Override
	public ScreenProcessor<?> screenProcessor() {
		return processor;
	}

	private Component trim(Component text, int maxWidth) {
		String value = text.getString();
		if (font.width(value) <= maxWidth) return text;

		int ellipsisWidth = font.width("...");
		return Component.literal(font.plainSubstrByWidth(value, Math.max(0, maxWidth - ellipsisWidth)) + "...");
	}

	private class ColumnHeader extends AbstractWidget {
		private final long count;

		private ColumnHeader(int width, Component message, long count) {
			super(0, 0, width, COLUMN_HEADER_HEIGHT, message);
			this.count = count;
			this.active = false;
		}

		@Override
		protected void extractWidgetRenderState(
				@NonNull GuiGraphicsExtractor graphics,
				int mouseX,
				int mouseY,
				float partialTick
		) {
			String countLabel = Long.toString(count);
			int countWidth = font.width(countLabel);
			int textY = getY() + 3;
			Component heading = trim(
					getMessage().copy().withStyle(ChatFormatting.BOLD),
					getWidth() - countWidth - 24
			);
			graphics.text(font, heading, getX() + 2, textY, 0xFFFFFFFF);
			graphics.text(font, countLabel, getRight() - countWidth - 2, textY, 0xFF909090);
			graphics.fill(getX(), getBottom() - 3, getRight(), getBottom() - 2, 0x88555555);
			graphics.fill(getX(), getBottom() - 3, getX() + Math.min(40, getWidth()), getBottom() - 2, 0xFFFFAA00);
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		}
	}

	private class SectionDivider extends AbstractWidget {
		private final boolean equipped;

		private SectionDivider(int width, Component message, boolean equipped) {
			super(0, 0, width, SECTION_HEIGHT, message);
			this.equipped = equipped;
		}

		@Override
		protected void extractWidgetRenderState(
				@NonNull GuiGraphicsExtractor graphics,
				int mouseX,
				int mouseY,
				float partialTick
		) {
			Component label = trim(getMessage(), getWidth() - 20);
			int textY = getY() + (getHeight() - font.lineHeight) / 2;
			int lineY = getY() + getHeight() / 2;
			int textWidth = font.width(label);
			boolean columnFocused = getFocused() == (equipped ? equippedContainer : availableContainer);
			boolean focused = isFocused() && columnFocused;
			if (focused) {
				graphics.fill(getX(), getY() + 1, getRight(), getBottom() - 1, 0x88303030);
				graphics.fill(getX() + 1, getY() + 2, getX() + 3, getBottom() - 2, 0xFFFFCC44);
			}
			graphics.text(font, label, getX() + 5, textY, focused ? 0xFFFFFFFF : 0xFFFFAA00);
			graphics.fill(
					getX() + textWidth + 12,
					lineY,
					getRight() - 5,
					lineY + 1,
					focused ? 0xFFAAAAAA : 0x88555555
			);
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, getMessage());
		}
	}

	private class EmptyState extends AbstractWidget {
		private EmptyState(int width, Component message) {
			super(0, 0, width, ACTION_HEIGHT * 2, message);
			this.active = false;
		}

		@Override
		protected void extractWidgetRenderState(
				@NonNull GuiGraphicsExtractor graphics,
				int mouseX,
				int mouseY,
				float partialTick
		) {
			graphics.centeredText(
					font,
					trim(getMessage(), getWidth() - 16),
					getX() + getWidth() / 2,
					getY() + (getHeight() - font.lineHeight) / 2,
					0xFF777777
			);
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		}
	}

	private class ActionEntry extends AbstractButton {
		private final Identifier id;
		private final boolean equipped;
		private final int position;
		private final RadialIcon icon;

		private ActionEntry(Identifier id, Component name, boolean equipped, int position, int width) {
			super(0, 0, width, ACTION_HEIGHT, name);
			this.id = id;
			this.equipped = equipped;
			this.position = position;

			InputBinding binding = candidatesById.get(id);
			this.icon = binding == null
					? RadialIcon.EMPTY
					: RadialIconManager.INSTANCE.getIcon(binding);
		}

		@Override
		public void onPress(InputWithModifiers input) {
			activate(id, equipped);
		}

		@Override
		protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			boolean columnFocused = getFocused() == (equipped ? equippedContainer : availableContainer);
			boolean focused = isFocused() && columnFocused;
			boolean carried = Objects.equals(id, model.carried());
			int background = focused
					? 0xDD303030
					: isHovered() ? 0xCC252525 : 0xAA101010;
			int border = carried
					? 0xFFFFCC44
					: focused ? 0xFFFFFFFF : isHovered() ? 0xFFAAAAAA : 0xFF555555;

			graphics.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, background);
			graphics.outline(getX(), getY(), getWidth(), getHeight(), border);
			if (carried) {
				graphics.fill(getX() + 2, getY() + 2, getX() + 5, getBottom() - 2, 0xFFFFCC44);
			} else if (equipped) {
				graphics.fill(getX() + 2, getY() + 2, getX() + 4, getBottom() - 2, 0xFF5599DD);
			}

			RadialIconExtractor.extract(graphics, icon, getX() + 7, getY() + 5);
			int textX = getX() + 28;
			int rightPadding = equipped ? 27 : 8;
			int textY = getY() + (getHeight() - font.lineHeight) / 2;
			int textColor = carried ? 0xFFFFDD66 : 0xFFFFFFFF;
			graphics.text(font, trim(getMessage(), getRight() - textX - rightPadding), textX, textY, textColor);

			if (equipped) {
				String slot = Integer.toString(position % 8 + 1);
				graphics.text(font, slot, getRight() - font.width(slot) - 8, textY, 0xFF888888);
			}
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}
	}

	private class TransferTarget extends AbstractButton {
		private final boolean equipped;
		private final Runnable action;

		private TransferTarget(int width, Component message, boolean equipped, Runnable action) {
			super(0, 0, width, ACTION_HEIGHT, message);
			this.equipped = equipped;
			this.action = action;
		}

		@Override
		public void onPress(InputWithModifiers input) {
			action.run();
		}

		@Override
		protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			boolean columnFocused = getFocused() == (equipped ? equippedContainer : availableContainer);
			boolean focused = isFocused() && columnFocused;
			int background = focused
					? 0xBB303030
					: isHovered() ? 0xAA252525 : 0x66101010;
			int border = focused ? 0xFFFFFFFF : isHovered() ? 0xFFAAAAAA : 0xFF777777;
			graphics.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, background);
			graphics.outline(getX(), getY(), getWidth(), getHeight(), border);
			graphics.centeredText(
					font,
					trim(getMessage(), getWidth() - 16),
					getX() + getWidth() / 2,
					getY() + (getHeight() - font.lineHeight) / 2,
					0xFFFFCC44
			);
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}
	}

	public static class Processor extends ScreenProcessor<RadialMenuEditScreen> {
		public Processor(RadialMenuEditScreen screen) {
			super(screen);
		}

		@Override
		protected void handleComponentNavigation(ControllerEntity controller) {
			if (!screen.model.isCarrying()) {
				super.handleComponentNavigation(controller);
				return;
			}

			boolean moved = false;
			if (ControlifyBindings.GUI_NAVI_LEFT.on(controller).justPressed()) {
				moved = screen.moveCarried(ScreenDirection.LEFT);
			} else if (ControlifyBindings.GUI_NAVI_RIGHT.on(controller).justPressed()) {
				moved = screen.moveCarried(ScreenDirection.RIGHT);
			} else if (ControlifyBindings.GUI_NAVI_UP.on(controller).justPressed()) {
				moved = screen.moveCarried(ScreenDirection.UP);
			} else if (ControlifyBindings.GUI_NAVI_DOWN.on(controller).justPressed()) {
				moved = screen.moveCarried(ScreenDirection.DOWN);
			}

			if (moved) {
				playFocusChangeSound();
				controller.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));
			}
		}

		@Override
		protected void handleButtons(ControllerEntity controller) {
			if (screen.model.isCarrying()) {
				if (ControlifyBindings.GUI_PRESS.on(controller).guiPressed().get()) {
					screen.dropCarried();
					playClackSound();
					return;
				}
			}
			super.handleButtons(controller);
		}

		@Override
		public VirtualMouseBehaviour virtualMouseBehaviour() {
			return VirtualMouseBehaviour.DISABLED;
		}
	}
}
