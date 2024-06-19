//? if fabric && fapi: >=0.100.0 {
package dev.isxander.controlify.platform.client.fabric;

import dev.isxander.controlify.platform.client.CreativeTabHelper;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

import java.util.List;

public class FAPIApiCreativeTabHelper implements CreativeTabHelper {
    private final CreativeModeInventoryScreen screen;

    public FAPIApiCreativeTabHelper(CreativeModeInventoryScreen screen) {
        this.screen = screen;
    }

    @Override
    public void setCurrentPage(int page) {
        screen.switchToPage(page);
    }

    @Override
    public int getCurrentPage() {
        return screen.getCurrentPage();
    }

    @Override
    public int getPageCount() {
        return screen.getPageCount();
    }

    @Override
    public List<CreativeModeTab> getTabsForPage(int page) {
        return screen.getItemGroupsOnPage(page);
    }

    @Override
    public CreativeModeTab getSelectedTab() {
        return screen.getSelectedItemGroup();
    }

    @Override
    public void setSelectedTab(CreativeModeTab tab) {
        screen.setSelectedItemGroup(tab);
    }
}
//?}
