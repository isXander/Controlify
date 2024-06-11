//? if neoforge {
/*package dev.isxander.controlify.platform.client.neoforge;

import dev.isxander.controlify.platform.client.CreativeTabHelper;
import dev.isxander.controlify.platform.neoforge.mixins.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;

import java.util.List;

public class NeoforgeCreativeTabHelper implements CreativeTabHelper {
    private final CreativeModeInventoryScreen screen;

    public NeoforgeCreativeTabHelper(CreativeModeInventoryScreen screen) {
        this.screen = screen;
    }

    @Override
    public void setCurrentPage(int page) {
        screen.setCurrentPage(getPages().get(page));
    }

    @Override
    public int getCurrentPage() {
        return getPages().indexOf(screen.getCurrentPage());
    }

    @Override
    public int getPageCount() {
        return getPages().size();
    }

    @Override
    public List<CreativeModeTab> getTabsForPage(int page) {
        return screen.getCurrentPage().getVisibleTabs();
    }

    @Override
    public CreativeModeTab getSelectedTab() {
        return CreativeModeInventoryScreenAccessor.getSelectedTab();
    }

    @Override
    public void setSelectedTab(CreativeModeTab tab) {
        ((CreativeModeInventoryScreenAccessor) screen).invokeSelectTab(tab);
    }

    private List<CreativeTabsScreenPage> getPages() {
        return ((CreativeModeInventoryScreenAccessor) screen).getPages();
    }
}
*///?}
