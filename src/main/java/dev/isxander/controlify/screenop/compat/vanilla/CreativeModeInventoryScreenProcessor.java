package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.screenop.vanilla.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTabs;

public class CreativeModeInventoryScreenProcessor extends ScreenProcessor<CreativeModeInventoryScreen> {
    public CreativeModeInventoryScreenProcessor(CreativeModeInventoryScreen screen) {
        super(screen);
    }

    @Override
    protected void handleVMouseNavigation(Controller controller) {
        var accessor = (CreativeModeInventoryScreenAccessor) screen;

        if (controller.bindings().GUI_NEXT_TAB.justPressed()) {
            var tabs = CreativeModeTabs.tabs();
            int newIndex = tabs.indexOf(accessor.getSelectedTab()) + 1;
            if (newIndex >= tabs.size()) newIndex = 0;
            accessor.invokeSelectTab(tabs.get(newIndex));
        }
        if (controller.bindings().GUI_PREV_TAB.justPressed()) {
            var tabs = CreativeModeTabs.tabs();
            int newIndex = tabs.indexOf(accessor.getSelectedTab()) - 1;
            if (newIndex < 0) newIndex = tabs.size() - 1;
            accessor.invokeSelectTab(tabs.get(newIndex));
        }
    }
}
