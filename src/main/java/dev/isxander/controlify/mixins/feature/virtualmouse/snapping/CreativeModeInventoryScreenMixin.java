package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.virtualmouse.SnapPoint;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreenMixin<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow protected abstract int getTabX(CreativeModeTab group);
    @Shadow private float scrollOffs;
    @Shadow private EditBox searchBox;
    @Shadow protected abstract boolean canScroll();

    protected CreativeModeInventoryScreenMixin(Component title) {
        super(title);
    }

    @Override
    public Set<SnapPoint> getSnapPoints() {
        Set<SnapPoint> points = super.getSnapPoints();
        for (var tab : CreativeModeTabs.tabs()) {
            boolean topRow = tab.row() == CreativeModeTab.Row.TOP;
            int x = leftPos + getTabX(tab);
            int y = topPos + (topRow ? -28 : imageHeight - 4);

            points.add(new SnapPoint(new Vector2i(x + 13, y + 16), 18));
        }

        if (canScroll()) {
            int scrollTop = topPos + 18;
            int scrollBottom = scrollTop + 112;
            points.add(new SnapPoint(new Vector2i(leftPos + 175 + 6, scrollTop + (int)((float)(scrollBottom - scrollTop - 17) * scrollOffs) + 7), 15));
        }

        if (searchBox.isVisible())
            points.add(new SnapPoint(new Vector2i(searchBox.getX() + searchBox.getWidth() / 2, searchBox.getY() + searchBox.getHeight() / 2), searchBox.getHeight() + 2));

        return points;
    }
}
