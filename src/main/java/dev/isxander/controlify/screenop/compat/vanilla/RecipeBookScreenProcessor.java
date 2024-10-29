package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookPageAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;

import java.util.List;

//? if >=1.21.2
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;

public class RecipeBookScreenProcessor
        <T extends /*? if >=1.21.2 {*/ AbstractRecipeBookScreen<?> /*?} else {*/ /*Screen & RecipeUpdateListener *//*?}*/>
        extends ScreenProcessor<T> {
    private final T screen;

    public RecipeBookScreenProcessor(T screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    protected void handleScreenVMouse(ControllerEntity controller, VirtualMouseHandler vmouse) {
        super.handleButtons(controller);

        //? if >=1.21.2 {
        RecipeBookComponent<?> recipeBookComponent = ((RecipeBookScreenAccessor) screen).getRecipeBookComponent();
        //?} else {
        /*RecipeBookComponent recipeBookComponent = screen.getRecipeBookComponent();
        *///?}

        if (!recipeBookComponent.isVisible()) return;
        RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
        RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.getRecipeBookPage();
        List<RecipeBookTabButton> tabs = componentAccessor
                .getTabButtons()
                .stream().filter(tab -> tab.visible)
                .toList();
        RecipeBookTabButton selectedTab = componentAccessor.getSelectedTab();

        StateSwitchingButton button;
        if (ControlifyBindings.VMOUSE_PAGE_NEXT.on(controller).justPressed()) {
            button = pageAccessor.getForwardButton();
            recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
        }
        if (ControlifyBindings.VMOUSE_PAGE_PREV.on(controller).justPressed()) {
            button = pageAccessor.getBackButton();
            recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
        }
        if (ControlifyBindings.VMOUSE_PAGE_DOWN.on(controller).justPressed()) {
            int index = tabs.indexOf(selectedTab);
            if (index != tabs.size() - 1) {
                button = tabs.get(index + 1);
                recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
            }
        }
        if (ControlifyBindings.VMOUSE_PAGE_UP.on(controller).justPressed()) {
            int index = tabs.indexOf(selectedTab);
            if (index != 0) {
                button = tabs.get(index - 1);
                recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
            }
        }
    }

    //? if >=1.21.2 {
    public interface RecipeBookScreenAccessor {
        RecipeBookComponent<?> getRecipeBookComponent();
    }
    //?}
}
