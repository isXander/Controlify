/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.container;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.Controlify;
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
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements ContainerScreenBehaviour.Access {
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
	@Final
	private List<ItemSlotMouseAction> itemSlotMouseActions;

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
}
