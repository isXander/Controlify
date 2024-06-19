package dev.isxander.controlify.platform.client;

import net.minecraft.world.item.CreativeModeTab;

import java.util.List;

public interface CreativeTabHelper {
    void setCurrentPage(int page);

    int getCurrentPage();

    int getPageCount();

    List<CreativeModeTab> getTabsForPage(int page);

    CreativeModeTab getSelectedTab();

    void setSelectedTab(CreativeModeTab tab);
}
