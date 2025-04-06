package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.ISnapBehaviour;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreenMixin<EnchantmentMenu> implements ISnapBehaviour {

    @Unique private static final int SLOT_AREA_OFFSET_X = 60, SLOT_AREA_OFFSET_Y = 14;
    @Unique private static final int SLOT_WIDTH = 108, SLOT_HEIGHT = 19;
    @Unique private static final int SLOT_VERTICAL_SPACING = SLOT_HEIGHT;

    // This math is based on the renderBg method in EnchantmentScreen
    @Override
    public void controlify$collectSnapPoints(Consumer<SnapPoint> consumer) {
        super.controlify$collectSnapPoints(consumer);

        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        int snapRadius = (SLOT_HEIGHT / 2) + 5;

        // enchanting table has 3 options
        for (int slotIndex = 0; slotIndex < 3; slotIndex++) {
            if (this.menu.costs[slotIndex] == 0) {
                // Cost is 0 if there is no enchantment in this slot, skip
                continue;
            }

            // Purposefully include the slots even if the player can't afford them,
            // since you can still hover over them to see the enchantment and its cost.
            int slotX = leftPos + SLOT_AREA_OFFSET_X;
            int slotY = topPos + SLOT_AREA_OFFSET_Y + (slotIndex * SLOT_VERTICAL_SPACING);

            int centreSlotX = slotX + (SLOT_WIDTH / 2);
            int centreSlotY = slotY + (SLOT_HEIGHT / 2);

            // Add the snap point for this slot
            consumer.accept(new SnapPoint(centreSlotX, centreSlotY, snapRadius));
        }
    }
}
