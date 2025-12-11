package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookPageAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

//? if >=1.21.2
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.world.inventory.Slot;

public class RecipeBookScreenProcessor
        <T extends /*? if >=1.21.2 {*/ AbstractRecipeBookScreen<?> /*?} else {*/ /*AbstractContainerScreen<?> *//*?}*/>
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

        RecipeBookComponent/*? if >=1.21.2 {*/<?>/*?}*/ recipeBookComponent = recipeBookScreenAccessor.controlify$getRecipeBookComponent();

        if (!recipeBookComponent.isVisible()) return;
        RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
        RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.getRecipeBookPage();
        List<RecipeBookTabButton> tabs = componentAccessor
                .getTabButtons()
                .stream().filter(tab -> tab.visible)
                .toList();
        RecipeBookTabButton selectedTab = componentAccessor.getSelectedTab();

        //? if >=1.21.11 {
        net.minecraft.client.gui.components.ImageButton button = null;
        //?} else {
        /*net.minecraft.client.gui.components.StateSwitchingButton button = null;
        *///?}
        if (ControlifyBindings.VMOUSE_PAGE_NEXT.on(controller).justPressed()) {
            button = pageAccessor.getForwardButton();
        }
        if (ControlifyBindings.VMOUSE_PAGE_PREV.on(controller).justPressed()) {
            button = pageAccessor.getBackButton();
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
            //? if >=1.21.9 {
            recipeBookComponent.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(
                    button.getX(), button.getY(),
                    new net.minecraft.client.input.MouseButtonInfo(0, 0)), false);
            //?} else {
            /*recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
            *///?}
        }
    }

    public interface RecipeBookScreenAccessor {
        RecipeBookComponent/*? if >=1.21.2 {*/<?>/*?}*/ controlify$getRecipeBookComponent();
    }
}
