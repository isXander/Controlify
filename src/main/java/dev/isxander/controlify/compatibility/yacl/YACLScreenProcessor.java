package dev.isxander.controlify.compatibility.yacl;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.compat.yacl.YACLScreenCategoryTabAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.tab.ListHolderWidget;

public class YACLScreenProcessor extends ScreenProcessor<YACLScreen> {
    public YACLScreenProcessor(YACLScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            if (screen.tabManager.getCurrentTab() instanceof YACLScreen.CategoryTab categoryTab) {
                ((YACLScreenCategoryTabAccessor) categoryTab).getSaveFinishedButton().onPress();
            }
            playClackSound();
        }

        super.handleButtons(controller);
    }

    @Override
    protected void onTabChanged(ControllerEntity controller) {
        if (screen.tabManager.getCurrentTab() instanceof YACLScreen.CategoryTab categoryTab) {
            ListHolderWidget<OptionListWidget> optionListHolder = ((YACLScreenCategoryTabAccessor) categoryTab).getOptionList();
            OptionListWidget optionList = optionListHolder.getList();
            optionList.setScrollAmount(0);

            screen.setFocused(optionListHolder);
            optionListHolder.setFocused(optionList);

            for (OptionListWidget.Entry entry : optionList.children()) {
                entry.setFocused(false);
                entry.setFocused(null);
            }

            OptionListWidget.Entry firstEntry = optionList.children().get(0);
            optionList.setFocused(firstEntry);

            firstEntry.setFocused(firstEntry.children().get(0));
        }
    }
}
