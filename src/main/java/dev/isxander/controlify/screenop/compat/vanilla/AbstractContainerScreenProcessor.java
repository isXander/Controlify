package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.function.Supplier;

public class AbstractContainerScreenProcessor<T extends AbstractContainerScreen<?>> extends ScreenProcessor<T> {
    private final Supplier<Slot> hoveredSlot;
    private final ClickSlotFunction clickSlotFunction;

    public AbstractContainerScreenProcessor(T screen, Supplier<Slot> hoveredSlot, ClickSlotFunction clickSlotFunction) {
        super(screen);
        this.hoveredSlot = hoveredSlot;
        this.clickSlotFunction = clickSlotFunction;
    }

    @Override
    protected void handleScreenVMouse(Controller<?, ?> controller) {
        if (controller.bindings().DROP.justPressed()) {
            Slot slot = hoveredSlot.get();
            if (slot != null && slot.hasItem()) {
                clickSlotFunction.clickSlot(slot, slot.index, 0, ClickType.THROW);
            }
        }
    }

    @FunctionalInterface
    public interface ClickSlotFunction {
        void clickSlot(Slot slot, int slotId, int button, ClickType clickType);
    }
}
