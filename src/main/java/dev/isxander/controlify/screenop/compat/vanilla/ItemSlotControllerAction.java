//? if >=1.21.2 {
package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.item.ItemStack;

public interface ItemSlotControllerAction extends ItemSlotMouseAction {
    boolean controlify$onControllerInput(
            ItemStack stack,
            int hoveredSlotIndex,
            ControllerEntity controller
    );
}
//?}
