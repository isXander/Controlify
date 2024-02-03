package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.CreativeModeInventoryScreenAccessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.fabricmc.fabric.impl.client.itemgroup.CreativeGuiExtensions;
import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage") // TODO: requires FAPI impl stuff to get pages working. will be changed in future FAPI update (hopefully)
public class CreativeModeInventoryScreenProcessor extends AbstractContainerScreenProcessor<CreativeModeInventoryScreen> {
    public CreativeModeInventoryScreenProcessor(CreativeModeInventoryScreen screen, Supplier<Slot> hoveredSlot, ClickSlotFunction clickSlotFunction) {
        super(screen, hoveredSlot, clickSlotFunction);
    }

    @Override
    protected void handleScreenVMouse(Controller<?> controller, VirtualMouseHandler vmouse) {
        var accessor = (CreativeModeInventoryScreenAccessor) screen;
        var ext = (CreativeGuiExtensions) screen;

        var tabs = getTabsOnCurrentPage(ext);
        if (controller.bindings().GUI_NEXT_TAB.justPressed()) {
            int newIndex = tabs.indexOf(CreativeModeInventoryScreenAccessor.getSelectedTab()) + 1;
            if (newIndex >= tabs.size()) {
                newIndex = 0;

                int currentPage = ext.fabric_currentPage();
                ext.fabric_nextPage();
                if (ext.fabric_currentPage() == currentPage) {
                    for (int i = 0; i < currentPage; i++) {
                        ext.fabric_previousPage();
                    }
                }
            }
            accessor.invokeSelectTab(getTabsOnCurrentPage(ext).get(newIndex));
        }
        if (controller.bindings().GUI_PREV_TAB.justPressed()) {
            int newIndex = tabs.indexOf(CreativeModeInventoryScreenAccessor.getSelectedTab()) - 1;
            if (newIndex < 0) {
                if (ext.fabric_currentPage() == 0) {
                    for (int i = 0; i < getLastPage(ext); i++) {
                        ext.fabric_nextPage();
                    }
                } else {
                    ext.fabric_previousPage();
                }

                newIndex = getTabsOnCurrentPage(ext).size() - 1;
            }
            accessor.invokeSelectTab(getTabsOnCurrentPage(ext).get(newIndex));
        }


        super.handleScreenVMouse(controller, vmouse);
    }

    private static List<CreativeModeTab> getTabsOnCurrentPage(CreativeGuiExtensions ext) {
        return CreativeModeTabs.tabs().stream() // just gets visible tabs from registry (excludes operator tab if disabled)
                .filter(tab -> ext.fabric_currentPage() == getFabricPage(ext, tab)) // only want current page of tabs, not all of them
                .sorted(Comparator.comparing(CreativeModeTab::row).thenComparingInt(CreativeModeTab::column)) // in hash order from the registry.
                .sorted((a, b) -> {
                    // make stuff aligned to the right go last
                    if (a.isAlignedRight() && !b.isAlignedRight()) return 1;
                    if (!a.isAlignedRight() && b.isAlignedRight()) return -1;
                    return 0;
                })
                .toList();
    }

    // straight up ripped from fapi impl
    private static int getFabricPage(CreativeGuiExtensions ext, CreativeModeTab tab) {
        if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(tab)) {
            return ext.fabric_currentPage();
        }

        final FabricItemGroup fabricItemGroup = (FabricItemGroup) tab;
        return fabricItemGroup.getPage();
    }

    // this is the only way to get the last page of tabs, since fapi doesn't expose it
    private static int getLastPage(CreativeGuiExtensions ext) {
        int lastPage = 0;
        while (hasGroupForPage(ext, lastPage)) {
            lastPage++;
        }
        return lastPage - 1;
    }

    private static boolean hasGroupForPage(CreativeGuiExtensions ext, int page) {
        return CreativeModeTabs.tabs().stream()
                .anyMatch(itemGroup -> getFabricPage(ext, itemGroup) == page);
    }
}
