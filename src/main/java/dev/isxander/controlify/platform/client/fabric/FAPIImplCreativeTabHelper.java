//? if fabric && fapi: <0.100.0 {
/*package dev.isxander.controlify.platform.client.fabric;

import dev.isxander.controlify.platform.client.CreativeTabHelper;
import dev.isxander.controlify.platform.fabric.mixins.CreativeModeInventoryScreenAccessor;
import net.fabricmc.fabric.impl.client.itemgroup.CreativeGuiExtensions;
import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class FAPIImplCreativeTabHelper implements CreativeTabHelper {
    private final CreativeModeInventoryScreen screen;
    private final CreativeGuiExtensions guiExt;
    private final int pageCount;

    public FAPIImplCreativeTabHelper(CreativeModeInventoryScreen screen) {
        this.screen = screen;
        this.guiExt = (CreativeGuiExtensions) screen;

        int lastPage = 0;
        while (pageHasAnyTab(lastPage)) {
            lastPage++;
        }
        this.pageCount = lastPage;
    }

    @Override
    public void setCurrentPage(int page) {
        int pageDiff = page - getCurrentPage();
        if (pageDiff == 0) return;

        boolean forwards = pageDiff > 0;
        for (int i = 0; i < Math.abs(pageDiff); i++) {
            if (forwards) guiExt.fabric_nextPage();
            else guiExt.fabric_previousPage();
        }
    }

    @Override
    public int getCurrentPage() {
        return guiExt.fabric_currentPage();
    }

    @Override
    public int getPageCount() {
        return pageCount;
    }

    @Override
    public List<CreativeModeTab> getTabsForPage(int page) {
        return CreativeModeTabs.tabs().stream() // just gets visible tabs from registry (excludes operator tab if disabled)
                .filter(tab -> page == getPageForTab(tab)) // only want current page of tabs, not all of them
                .sorted(Comparator.comparing(CreativeModeTab::row).thenComparingInt(CreativeModeTab::column)) // in hash order from the registry.
                .sorted((a, b) -> {
                    // make stuff aligned to the right go last
                    if (a.isAlignedRight() && !b.isAlignedRight()) return 1;
                    if (!a.isAlignedRight() && b.isAlignedRight()) return -1;
                    return 0;
                })
                .toList();
    }

    private int getPageForTab(CreativeModeTab tab) {
        if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(tab)) {
            return getCurrentPage();
        }

        FabricItemGroup fabricItemGroup = (FabricItemGroup) tab;
        return fabricItemGroup.getPage();
    }

    @Override
    public CreativeModeTab getSelectedTab() {
        return CreativeModeInventoryScreenAccessor.getSelectedTab();
    }

    @Override
    public void setSelectedTab(CreativeModeTab tab) {
        ((CreativeModeInventoryScreenAccessor) screen).invokeSelectTab(tab);
    }

    private boolean pageHasAnyTab(int page) {
        return CreativeModeTabs.tabs().stream()
                .anyMatch(tab -> getPageForTab(tab) == page);
    }
}
*///?}
