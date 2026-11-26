/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.container;

import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.ContainerScreenBehaviour;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.ItemSlotControllerAction;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//? if >=26.4 {
import net.minecraft.client.gui.components.inventory.SlotGroup;
import net.minecraft.client.gui.components.inventory.SlotWidget;
//?} else {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.Controlify;
import org.spongepowered.asm.mixin.injection.At;
*///?}

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements ContainerScreenBehaviour.Access {
	//? if >=26.4 {
	// 26.4 turned every slot into a SlotWidget, which now owns hovering and its item action
	@Shadow
	@Final
	private List<SlotGroup> slotGroups;

	@Shadow
	protected abstract @Nullable SlotWidget getHoveredSlot();

	@Shadow
	protected abstract void slotClicked(@Nullable SlotWidget slotWidget, int slotId, int buttonNum, ContainerInput containerInput);

	@Shadow
	protected abstract boolean hasClickedOutside(double mx, double my);

	@Override
	public @Nullable Slot controlify$getHoveredSlot() {
		SlotWidget widget = this.getHoveredSlot();
		return widget != null ? widget.slot() : null;
	}

	@Override
	public boolean controlify$hasClickedOutside(double mouseX, double mouseY) {
		return this.hasClickedOutside(mouseX, mouseY);
	}

	@Override
	public void controlify$clickSlot(@Nullable Slot slot, int slotId, int button, @NonNull ContainerInput input) {
		this.slotClicked(this.controlify$findSlotWidget(slot), slotId, button, input);
	}

	@Override
	public boolean controlify$handleItemSlotActions(@NonNull ControllerImpl controller) {
		SlotWidget widget = this.getHoveredSlot();
		if (widget == null) {
			return false;
		}
		Slot slot = widget.slot();
		ItemSlotMouseAction action = ((ContainerScreenBehaviour.SlotWidgetAccess) widget).controlify$getItemSlotMouseAction();
		return action instanceof ItemSlotControllerAction controllerAction
				&& action.matches(slot)
				&& controllerAction.controlify$onControllerInput(slot.getItem(), slot.index, controller);
	}

	private @Nullable SlotWidget controlify$findSlotWidget(@Nullable Slot slot) {
		if (slot == null) {
			return null;
		}
		for (SlotGroup group : this.slotGroups) {
			for (SlotWidget widget : group.children()) {
				if (widget.slot() == slot) {
					return widget;
				}
			}
		}
		return null;
	}
	//?} else {
	/*@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

	@Shadow
	@Nullable protected Slot hoveredSlot;

	@Shadow
	protected abstract void slotClicked(
			Slot slot,
			int slotId,
			int buttonNum,
			ContainerInput containerInput
	);

	@Shadow
	protected abstract boolean hasClickedOutside(double mx, double my, int xo, int yo);

	@Shadow
	@Final
	private List<ItemSlotMouseAction> itemSlotMouseActions;

	@Override
	public @Nullable Slot controlify$getHoveredSlot() {
		return this.hoveredSlot;
	}

	@Override
	public boolean controlify$hasClickedOutside(double mouseX, double mouseY) {
		return this.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos);
	}

	@Override
	public void controlify$clickSlot(@Nullable Slot slot, int slotId, int button, @NonNull ContainerInput input) {
		this.slotClicked(slot, slotId, button, input);
	}

	@Override
	public boolean controlify$handleItemSlotActions(@NonNull ControllerImpl controller) {
		for (ItemSlotMouseAction itemSlotMouseAction : this.itemSlotMouseActions) {
			if (itemSlotMouseAction instanceof ItemSlotControllerAction itemSlotControllerAction
				&& itemSlotMouseAction.matches(this.hoveredSlot)
				&& itemSlotControllerAction.controlify$onControllerInput(this.hoveredSlot.getItem(), this.hoveredSlot.index, controller)) {
				return true;
			}
		}
		return false;
	}

	@ModifyExpressionValue(
			method = "mouseScrolled",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z"
			)
	)
	private boolean allowItemSlotScrolling(boolean original) {
		return original && !Controlify.instance().currentInputMode().isController();
	}
	*///?}
}
