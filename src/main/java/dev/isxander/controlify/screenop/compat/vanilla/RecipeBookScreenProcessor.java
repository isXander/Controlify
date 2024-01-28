package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.Controller;
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

public class RecipeBookScreenProcessor<T extends Screen> extends ScreenProcessor<T> {
    private final T screen;

    public RecipeBookScreenProcessor(T screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    protected void handleScreenVMouse(Controller<?, ?> controller, VirtualMouseHandler vmouse) {
        super.handleButtons(controller);

        RecipeBookComponent recipeBookComponent = ((RecipeUpdateListener) screen).getRecipeBookComponent();
        if (!recipeBookComponent.isVisible()) return;
        RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
        RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.getRecipeBookPage();
        List<RecipeBookTabButton> tabs = componentAccessor
                .getTabButtons()
                .stream().filter(tab -> tab.visible)
                .toList();
        RecipeBookTabButton selectedTab = componentAccessor.getSelectedTab();

        StateSwitchingButton button;
        if (controller.bindings().VMOUSE_PAGE_NEXT.justPressed()) {
            button = pageAccessor.getForwardButton();
            recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
        }
        if (controller.bindings().VMOUSE_PAGE_PREV.justPressed()) {
            button = pageAccessor.getBackButton();
            recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
        }
        if (controller.bindings().VMOUSE_PAGE_DOWN.justPressed()) {
            int index = tabs.indexOf(selectedTab);
            if (index != tabs.size() - 1) {
                button = tabs.get(index + 1);
                recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
            }
        }
        if (controller.bindings().VMOUSE_PAGE_UP.justPressed()) {
            int index = tabs.indexOf(selectedTab);
            if (index != 0) {
                button = tabs.get(index - 1);
                recipeBookComponent.mouseClicked(button.getX(), button.getY(), 0);
            }
        }
    }
}
