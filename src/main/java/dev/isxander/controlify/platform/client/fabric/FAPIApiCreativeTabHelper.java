//? if fabric {
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
        //? if >=26.1 {
        return screen.getTabsOnPage(page);
        //?} else {
        /*return screen.getItemGroupsOnPage(page);
        *///?}
    }

    @Override
    public CreativeModeTab getSelectedTab() {
        //? if >=26.1 {
        return screen.getSelectedTab();
        //?} else {
        /*return screen.getSelectedItemGroup();
        *///?}
    }

    @Override
    public void setSelectedTab(CreativeModeTab tab) {
        //? if >=26.1 {
        screen.setSelectedTab(tab);
        //?} else {
        /*screen.setSelectedItemGroup(tab);
        *///?}
    }
}
//?}
