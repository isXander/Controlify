package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookPageAccessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.inventory.Slot;

public class RecipeBookScreenProcessor<T extends AbstractRecipeBookScreen<?>>
        extends AbstractContainerScreenProcessor<T> {

    private final RecipeBookScreenAccessor recipeBookScreenAccessor;

    public RecipeBookScreenProcessor(
            T screen,
            RecipeBookScreenAccessor recipeBookScreenAccessor,
            Supplier<Slot> hoveredSlot,
            ClickSlotFunction clickSlotFunction,
            Predicate<ControllerEntity> doItemSlotActions
    ) {
        super(screen, hoveredSlot, clickSlotFunction, doItemSlotActions);
        this.recipeBookScreenAccessor = recipeBookScreenAccessor;
    }

    @Override
    protected void handleScreenVMouse(ControllerEntity controller, VirtualMouseHandler vmouse) {
        super.handleScreenVMouse(controller, vmouse);

        RecipeBookComponent<?> recipeBookComponent = recipeBookScreenAccessor.controlify$getRecipeBookComponent();

        if (!recipeBookComponent.isVisible()) return;
        RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
        RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.controlify$getRecipeBookPage();
        List<RecipeBookTabButton> tabs = componentAccessor
                .controlify$getTabButtons()
                .stream().filter(tab -> tab.visible)
                .toList();
        RecipeBookTabButton selectedTab = componentAccessor.controlify$getSelectedTab();

        ImageButton button = null;
        if (ControlifyBindings.VMOUSE_PAGE_NEXT.on(controller).justPressed()) {
            button = pageAccessor.controlify$getForwardButton();
        }
        if (ControlifyBindings.VMOUSE_PAGE_PREV.on(controller).justPressed()) {
            button = pageAccessor.controlify$getBackButton();
        }
        if (ControlifyBindings.VMOUSE_PAGE_DOWN.on(controller).justPressed()) {
            int index = tabs.indexOf(selectedTab);
            if (index != tabs.size() - 1) {
                button = tabs.get(index + 1);
            }
        }
        if (ControlifyBindings.VMOUSE_PAGE_UP.on(controller).justPressed()) {
            int index = tabs.indexOf(selectedTab);
            if (index != 0) {
                button = tabs.get(index - 1);
            }
        }
        if (button != null) {
            recipeBookComponent.mouseClicked(new MouseButtonEvent(
                    button.getX(), button.getY(),
                    new MouseButtonInfo(0, 0)), false);
        }
    }

    public interface RecipeBookScreenAccessor {
        RecipeBookComponent<?> controlify$getRecipeBookComponent();
    }
}
