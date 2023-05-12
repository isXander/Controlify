package dev.isxander.controlify.gui.guide;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ContainerGuideCtx(@Nullable Slot hoveredSlot, ItemStack holdingItem, boolean cursorOutsideContainer) {
}
