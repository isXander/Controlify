/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.container;

import org.spongepowered.asm.mixin.Mixin;

//? if >=26.4 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.ContainerScreenBehaviour;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.components.inventory.SlotWidget;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/// Slots became widgets in 26.4, each owning the item action (bundle selection) that the
/// container screen used to hold.
@Mixin(SlotWidget.class)
public abstract class SlotWidgetMixin implements ContainerScreenBehaviour.SlotWidgetAccess {
	@Shadow
	@Final
	private ItemSlotMouseAction itemSlotMouseAction;

	@Override
	public @NonNull ItemSlotMouseAction controlify$getItemSlotMouseAction() {
		return this.itemSlotMouseAction;
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
//?} else {
/*@Mixin(dev.isxander.controlify.impl.utils.DummyMixinTarget.class)
public abstract class SlotWidgetMixin {
}
*///?}
