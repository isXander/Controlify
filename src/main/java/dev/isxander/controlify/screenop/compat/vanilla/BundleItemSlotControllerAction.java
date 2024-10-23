//? if >=1.21.2 {
package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

public class BundleItemSlotControllerAction {
    public static boolean onControllerInput(
            ItemStack stack,
            int hoveredSlotIndex,
            ControllerEntity controller,
            SelectedBundleItemConsumer consumer
    ) {
        int uniqueItems = BundleItem.getNumberOfItemsToShow(stack);

        if (uniqueItems > 0) {
            Controlify.instance().virtualMouseHandler().preventScrollingThisTick();

            boolean up = ControlifyBindings.BUNDLE_NAVI_UP.on(controller).justPressed();
            boolean down = ControlifyBindings.BUNDLE_NAVI_DOWN.on(controller).justPressed();
            boolean left = ControlifyBindings.BUNDLE_NAVI_LEFT.on(controller).justPressed();
            boolean right = ControlifyBindings.BUNDLE_NAVI_RIGHT.on(controller).justPressed();

            int offsetX = 0, offsetY = 0;
            if (up) offsetY--;
            if (down) offsetY++;
            if (left) offsetX--;
            if (right) offsetX++;

            if (offsetX != 0 || offsetY != 0) {
                int currentIndex = BundleItem.getSelectedItem(stack);

                if (currentIndex == -1) {
                    consumer.accept(stack, hoveredSlotIndex, 0);
                    return true;
                }

                /*
                Bundle grid is organised where the first row is the incomplete row and is RTL,
                - - 0 1
                2 3 4 5
                 */
                int rowSize = BundleItem.MAX_SHOWN_GRID_ITEMS_X;
                int colSize = Math.min(Mth.ceil(uniqueItems / (float) rowSize), BundleItem.MAX_SHOWN_GRID_ITEMS_Y);
                int incompleteRowSize = uniqueItems % rowSize;
                int emptySlots = (rowSize - incompleteRowSize) % rowSize;

                System.out.println("rowSize: " + rowSize + ", colSize: " + colSize + ", incompleteRowSize: " + incompleteRowSize);

                int gridX = (currentIndex + emptySlots) % rowSize;
                int gridY = (currentIndex + emptySlots) / rowSize;

                int newGridX = (gridX + offsetX + rowSize) % rowSize;
                int newGridY = (gridY + offsetY + colSize) % colSize;

                System.out.println("currentIndex: " + currentIndex + ", gridX: " + gridX + ", gridY: " + gridY + ", newGridX: " + newGridX + ", newGridY: " + newGridY);

                if (newGridY >= 0 && newGridY < colSize) {
                    int newIndex = Math.max(newGridX + newGridY * rowSize - emptySlots, 0);
                    System.out.println("newIndex: " + newIndex);

                    consumer.accept(stack, hoveredSlotIndex, newIndex);
                }

                return true;
            }
        }

        return false;
    }

    public interface SelectedBundleItemConsumer {
        void accept(ItemStack stack, int hoveredSlot, int selectedIndex);
    }
}
//?}
