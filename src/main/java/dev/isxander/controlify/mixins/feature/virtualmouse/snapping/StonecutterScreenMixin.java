package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.ISnapBehaviour;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.world.inventory.StonecutterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

@Mixin(StonecutterScreen.class)
public abstract class StonecutterScreenMixin extends AbstractContainerScreenMixin<StonecutterMenu> implements ISnapBehaviour {

    @Unique private static final int COLUMNS = 4, ROWS = 3, SLOTS = COLUMNS * ROWS;
    @Unique private static final int BTN_WIDTH = 16, BTN_HEIGHT = 18;
    @Unique private static final int BTN_Y_PADDING = 2;

    @Shadow private int startIndex;

    // This math is based on the renderBg method in StonecutterScreen
    @Override
    public void controlify$collectSnapPoints(Consumer<SnapPoint> consumer) {
        super.controlify$collectSnapPoints(consumer);

        int startIndex = this.startIndex;
        int endIndex = this.startIndex + SLOTS;
        int visibleRecipes = this.menu.getNumberOfVisibleRecipes();

        int gridLeft = this.leftPos + 52;
        int gridTop = this.topPos + 14;

        int halfWidth = BTN_WIDTH / 2;
        int halfHeight = BTN_HEIGHT / 2;
        int snapRadius = halfHeight + 2;

        // Iterate through the range of absolute indices that are considered visible
        for (int absoluteIndex = startIndex; absoluteIndex < endIndex && absoluteIndex < visibleRecipes + startIndex; absoluteIndex++) {
            // Calculate the index relative to the start of the visible elements (0, 1, 2...)
            int relativeIndex = absoluteIndex - startIndex;

            // Determine the row and column based on the visible index
            int column = relativeIndex % COLUMNS;
            int row = relativeIndex / COLUMNS; // integer division

            // Calculate centre X of this slot
            int posX = gridLeft + column * BTN_WIDTH + halfWidth;

            // Calculate the top-left Y coordinate of this slot
            int posY = gridTop + row * BTN_HEIGHT + BTN_Y_PADDING + halfHeight;

            // Add the snap point for this slot
            consumer.accept(new SnapPoint(posX, posY, snapRadius));
        }
    }
}
