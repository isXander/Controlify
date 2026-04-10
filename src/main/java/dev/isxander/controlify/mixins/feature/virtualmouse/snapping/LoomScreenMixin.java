package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.ISnapBehaviour;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Consumer;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin extends AbstractContainerScreenMixin<LoomMenu> implements ISnapBehaviour {

    @Unique private static final int COLUMNS = 4, ROWS = 4, SLOTS = COLUMNS * ROWS;
    @Unique private static final int BTN_SIZE = 14;

    @Shadow private boolean displayPatterns;
    @Shadow private int startRow;

    // This math is based on the renderBg method in LoomScreen
    @Override
    public void controlify$collectSnapPoints(Consumer<SnapPoint> consumer) {
        super.controlify$collectSnapPoints(consumer);

        if (!this.displayPatterns) {
            return;
        }
        // Get list of available patterns
        List<Holder<BannerPattern>> patterns = this.menu.getSelectablePatterns();

        // Early exit if no patterns are available
        if (patterns.isEmpty()) {
            return;
        }

        int numPatterns = patterns.size();

        int gridLeft = this.leftPos + 60;
        int gridTop = this.topPos + 14;

        // Determine the range of *absolute* pattern indices to consider.
        // The first index corresponds to the top-left corner of the grid.
        int firstIndex = this.startRow * COLUMNS;

        // The last potential index is SLOTS slots after the first one,
        // but cannot exceed the number of available patterns
        int endPatternIndexExclusive = Math.min(
                numPatterns,       // cannot exceed the number of available patterns
                firstIndex + SLOTS // cannot exceed the number of slots in the grid
        );

        for (int patternIndex = firstIndex; patternIndex < endPatternIndexExclusive; patternIndex++) {
            // Calculate the grid row and column based on the absolute index
            // These calculations effectively reverse the patternIndex = absoluteRow * COLS + col formula
            int absoluteRow = patternIndex / COLUMNS;
            int gridColumn = patternIndex % COLUMNS;

            // Calculate the row relative to the visible grid
            int gridRow = absoluteRow - this.startRow;

            // Calculate the screen position in the centre of the button
            int buttonX = gridLeft + gridColumn * BTN_SIZE + BTN_SIZE / 2;
            int buttonY = gridTop + gridRow * BTN_SIZE + BTN_SIZE / 2;

            // Add the snap point for this slot
            consumer.accept(new SnapPoint(buttonX, buttonY, BTN_SIZE / 2));
        }
    }
}
