package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.CreativeModeInventoryScreenAccessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.function.Supplier;

public class CreativeModeInventoryScreenProcessor extends AbstractContainerScreenProcessor<CreativeModeInventoryScreen> {
    public CreativeModeInventoryScreenProcessor(CreativeModeInventoryScreen screen, Supplier<Slot> hoveredSlot, ClickSlotFunction clickSlotFunction) {
        super(screen, hoveredSlot, clickSlotFunction);
    }

    @Override
    protected void handleScreenVMouse(Controller<?, ?> controller, VirtualMouseHandler vmouse) {
        var accessor = (CreativeModeInventoryScreenAccessor) screen;

        if (controller.bindings().GUI_NEXT_TAB.justPressed()) {
            var tabs = CreativeModeTabs.tabs();
            int newIndex = tabs.indexOf(CreativeModeInventoryScreenAccessor.getSelectedTab()) + 1;
            if (newIndex >= tabs.size()) newIndex = 0;
            accessor.invokeSelectTab(tabs.get(newIndex));
        }
        if (controller.bindings().GUI_PREV_TAB.justPressed()) {
            var tabs = CreativeModeTabs.tabs();
            int newIndex = tabs.indexOf(CreativeModeInventoryScreenAccessor.getSelectedTab()) - 1;
            if (newIndex < 0) newIndex = tabs.size() - 1;
            accessor.invokeSelectTab(tabs.get(newIndex));
        }

        super.handleScreenVMouse(controller, vmouse);
    }
}
