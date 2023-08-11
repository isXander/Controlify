package dev.isxander.controlify.compatibility.yacl;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.yacl.YACLScreenCategoryTabAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.yacl3.gui.YACLScreen;

public class YACLScreenProcessor extends ScreenProcessor<YACLScreen> {
    public YACLScreenProcessor(YACLScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(Controller<?, ?> controller) {
        if (controller.bindings().GUI_ABSTRACT_ACTION_1.justPressed()) {
            if (screen.tabManager.getCurrentTab() instanceof YACLScreen.CategoryTab categoryTab) {
                ((YACLScreenCategoryTabAccessor) categoryTab).getSaveFinishedButton().onPress();
            }
            playClackSound();
        }

        super.handleButtons(controller);
    }
}
