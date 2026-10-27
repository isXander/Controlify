/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.controllers;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.Gesture;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.BindingConflicts;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.action.activator.LeafConfig;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.impl.gui.screen.BindConsumerScreen;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/// Edits all of an action's bindings in one row: the action name, then a button per binding slot.
public class BindController implements Controller<List<ActionBinding>> {
	public static final int SLOTS = 2;

	private final Option<List<ActionBinding>> option;
	public final @Nullable ControllerImpl controller;
	/// The leaf a binding may override the gesture of, if the action allows it.
	private final Optional<LeafConfig> editableLeaf;
	private final BindingConflicts.@Nullable Kind[] slotConflicts = new BindingConflicts.Kind[SLOTS];
	private List<Component> conflictLines = List.of();

	public BindController(Option<List<ActionBinding>> option, @Nullable ControllerImpl controller, Optional<LeafConfig> editableLeaf) {
		this.option = option;
		this.controller = controller;
		this.editableLeaf = editableLeaf;
	}

	@Override
	public Option<List<ActionBinding>> option() {
		return this.option;
	}

	@Override
	public Component formatValue() {
		return Component.empty();
	}

	/// @param slotConflicts the most severe conflict on each slot, or null for none
	/// @param conflictLines explanations shown in the description
	public void setConflicts(BindingConflicts.@Nullable Kind[] slotConflicts, List<Component> conflictLines) {
		System.arraycopy(slotConflicts, 0, this.slotConflicts, 0, SLOTS);
		this.conflictLines = List.copyOf(conflictLines);
	}

	public List<Component> conflictLines() {
		return this.conflictLines;
	}

	public Optional<ActionBinding> slot(int slot) {
		List<ActionBinding> bindings = this.option.pendingValue();
		return slot < bindings.size() ? Optional.of(bindings.get(slot)) : Optional.empty();
	}

	/// Clearing a slot moves later bindings up, so filled slots always come first.
	public void setSlot(int slot, Optional<ActionBinding> binding) {
		List<ActionBinding> bindings = new ArrayList<>(this.option.pendingValue());
		if (slot < bindings.size()) {
			if (binding.isPresent()) {
				bindings.set(slot, binding.get());
			} else {
				bindings.remove(slot);
			}
		} else {
			binding.ifPresent(bindings::add);
		}
		this.option.requestSet(List.copyOf(bindings));
	}

	public boolean canEditGesture(int slot) {
		return this.editableLeaf.isPresent() && this.slot(slot).isPresent();
	}

	/// Steps through every gesture of the editable leaf's kind, starting from the one in effect.
	/// Choosing the leaf's own gesture stores no override.
	public void cycleGesture(int slot) {
		if (!this.canEditGesture(slot)) {
			return;
		}
		LeafConfig leaf = this.editableLeaf.get();
		Gesture defaultGesture = leaf.gesture().orElseThrow();
		List<Gesture> choices = Arrays.stream(Gesture.values())
				.filter(gesture -> gesture.kind() == defaultGesture.kind())
				// GUI press is tied to GUI navigation, which only GUI actions have.
				.filter(gesture -> gesture != Gesture.GUI_PRESS || gesture == defaultGesture)
				.toList();
		ActionBinding binding = this.slot(slot).orElseThrow();
		Gesture current = leaf.gestureFor(binding).orElse(defaultGesture);
		Gesture next = choices.get((choices.indexOf(current) + 1) % choices.size());
		this.setSlot(slot, Optional.of(binding.withGesture(next == defaultGesture ? Optional.empty() : Optional.of(next))));
	}

	private Component slotText(int slot) {
		Identifier fontNamespace = this.controller == null ? ControllerType.DEFAULT.id() : this.controller.info().type().id();
		return this.slot(slot)
				.map(binding -> Controlify.instance().inputFontMapper().getComponentFromBinding(fontNamespace, binding, this.editableLeaf))
				.orElseGet(() -> Component.literal("-").withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public BindControllerElement provideWidget(YACLScreen yaclScreen, Dimension<Integer> dimension) {
		return new BindControllerElement(this, yaclScreen, dimension);
	}

	public static class BindControllerElement extends ControllerWidget<BindController> implements ElementBehaviour {
		private static final int SLOT_GAP = 2;

		public boolean awaitingControllerInput = false;
		private final Component awaitingText = Component.translatable("controlify.gui.bind_input_awaiting").withStyle(ChatFormatting.ITALIC);
		/// The slot keyboard and controller input act on. The mouse acts on whichever slot it is over.
		private int selectedSlot = 0;

		public BindControllerElement(BindController control, YACLScreen screen, Dimension<Integer> dim) {
			super(control, screen, dim);
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			hovered = isMouseOver(mouseX, mouseY);

			// Unlike other options, the name sits outside any button so each slot reads as its own button.
			Component name = control.option().changed() ? modifiedOptionName : control.option().name();
			int nameWidth = getDimension().width() - getControlWidth() - getXPadding() * 2;
			Component shortenedName = Component.literal(GuiUtils.shortenString(name.getString(), textRenderer, nameWidth, "..."))
					.setStyle(name.getStyle());
			graphics.text(textRenderer, shortenedName, getDimension().x() + getXPadding(), getTextY(), isAvailable() ? -1 : inactiveColor, true);

			for (int slot = 0; slot < SLOTS; slot++) {
				int x1 = slotX(slot);
				int x2 = x1 + slotWidth();
				boolean highlighted = isOverSlot(slot, mouseX, mouseY) || focused && slot == selectedSlot;
				// The button sprite is greyscale, so tinting it marks conflicts without touching the glyphs,
				// which always render in their own colours.
				graphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						GuiUtils.BUTTON_SPRITES.get(isAvailable(), highlighted && isAvailable()),
						x1, getDimension().y(),
						x2 - x1, getDimension().height(),
						slotTint(slot)
				);

				Component text = awaitingControllerInput && slot == selectedSlot ? awaitingText : control.slotText(slot);
				Component shortened = textRenderer.width(text) > slotWidth() - 4
						? Component.literal(GuiUtils.shortenString(text.getString(), textRenderer, slotWidth() - 4, "...")).setStyle(text.getStyle())
						: text;
				int textX = x1 + (slotWidth() - textRenderer.width(shortened)) / 2;
				graphics.text(textRenderer, shortened, textX, getTextY() + 1, isAvailable() ? -1 : inactiveColor, true);
			}
		}

		private int slotWidth() {
			return Math.min(100, (getDimension().width() / 2 - SLOT_GAP) / SLOTS);
		}

		private int slotX(int slot) {
			int firstX = getDimension().xLimit() - SLOTS * slotWidth() - (SLOTS - 1) * SLOT_GAP;
			return firstX + slot * (slotWidth() + SLOT_GAP);
		}

		private boolean isOverSlot(int slot, double mouseX, double mouseY) {
			int x1 = slotX(slot);
			return mouseX >= x1 && mouseX < x1 + slotWidth()
					&& mouseY >= getDimension().y() && mouseY < getDimension().yLimit();
		}

		private int slotTint(int slot) {
			BindingConflicts.Kind conflict = control.slotConflicts[slot];
			if (conflict == null || !isAvailable()) {
				return -1;
			}
			return switch (conflict) {
				case CONFLICT -> 0xFFFF6060;
				case MODIFIER_LEAK -> 0xFFFFB040;
				case REDUNDANT, SHADOWED -> 0xFFA0A0A0;
			};
		}

		/// The area the bind capture screen leaves undimmed: the slot being rebound.
		public Dimension<Integer> awaitingSlotDimension() {
			return Dimension.ofInt(slotX(selectedSlot), getDimension().y(), slotWidth(), getDimension().height());
		}

		@Override
		public boolean keyPressed(KeyEvent keyEvent) {
			if (!isFocused()) {
				return false;
			}
			switch (keyEvent.key()) {
				case InputConstants.KEY_RETURN -> openConsumerScreen(selectedSlot);
				// Past either end, let focus move on, such as to the reset button.
				case InputConstants.KEY_LEFT -> {
					return moveSlot(-1);
				}
				case InputConstants.KEY_RIGHT -> {
					return moveSlot(1);
				}
				default -> {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
			for (int slot = 0; slot < SLOTS; slot++) {
				if (isOverSlot(slot, mouseButtonEvent.x(), mouseButtonEvent.y())) {
					selectedSlot = slot;
					if (mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
						control.cycleGesture(slot);
					} else {
						openConsumerScreen(slot);
					}
					return true;
				}
			}
			return false;
		}

		private void openConsumerScreen(int slot) {
			selectedSlot = slot;
			awaitingControllerInput = true;
			MinecraftUtil.setScreen(new BindConsumerScreen(
					control.controller != null ? control.controller : Controlify.instance().getCurrentController().orElse(null),
					control.slot(slot),
					binding -> control.setSlot(slot, binding),
					this,
					MinecraftUtil.getScreen()
			));
		}

		@Override
		public void handleNavigation(@NonNull ScreenContext<?> screen, dev.isxander.controlify.api.controller.@NonNull Controller controller, @NonNull Runnable next) {
			// Horizontal navigation moves between slots; the option list only scrolls vertically.
			// Past either end, the pulse is left for the screen to move focus on, such as to the reset button.
			if (controller == control.controller && focused) {
				if (selectedSlot > 0 && ControlifyActions.GUI_NAVI_LEFT.on(controller).pulse().consumeAllPulses()) {
					moveSlot(-1);
					return;
				}
				if (selectedSlot < SLOTS - 1 && ControlifyActions.GUI_NAVI_RIGHT.on(controller).pulse().consumeAllPulses()) {
					moveSlot(1);
					return;
				}
			}
			next.run();
		}

		@Override
		public void handleInteraction(@NonNull ScreenContext<?> screen, dev.isxander.controlify.api.controller.@NonNull Controller controller, @NonNull Runnable next) {
			if (controller != control.controller) {
				next.run();
				return;
			}

			if (ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses()) {
				openConsumerScreen(selectedSlot);
				return;
			}
			if (control.canEditGesture(selectedSlot) && ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
				control.cycleGesture(selectedSlot);
				return;
			}

			next.run();
		}

		/// @return whether the selection moved; false at either end
		private boolean moveSlot(int direction) {
			int target = selectedSlot + direction;
			if (target < 0 || target >= SLOTS) {
				return false;
			}
			selectedSlot = target;
			return true;
		}

		@Override
		public void setFocused(boolean focused) {
			super.setFocused(focused);
			// Each row starts on its first slot. Rebinding takes focus away briefly, so keep the slot then.
			if (!focused && !awaitingControllerInput) {
				selectedSlot = 0;
			}
		}

		@Override
		public void unfocus() {
			super.unfocus();
			if (!awaitingControllerInput) {
				selectedSlot = 0;
			}
		}

		@Override
		protected int getHoveredControlWidth() {
			return getUnhoveredControlWidth();
		}

		@Override
		protected int getUnhoveredControlWidth() {
			return SLOTS * slotWidth() + (SLOTS - 1) * SLOT_GAP;
		}
	}
}
