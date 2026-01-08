package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record ContainerCtx(
        @Nullable Slot hoveredSlot,
        ItemStack holdingItem,
        boolean cursorOutsideContainer,
        ControllerEntity controller,
        GuideVerbosity verbosity
) implements FactCtx {
}
