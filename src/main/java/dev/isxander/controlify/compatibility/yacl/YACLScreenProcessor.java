package dev.isxander.controlify.compatibility.yacl;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.yacl.gui.YACLScreen;

public class YACLScreenProcessor extends ScreenProcessor<YACLScreen> {
    public YACLScreenProcessor(YACLScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(Controller<?, ?> controller) {
        if (controller.bindings().GUI_ABSTRACT_ACTION_1.justPressed()) {
            this.playClackSound();
            screen.finishedSaveButton.onPress();
        }

        if (controller.bindings().GUI_NEXT_TAB.justPressed()) {
            var idx = screen.getCurrentCategoryIdx() + 1;
            if (idx >= screen.config.categories().size()) idx = 0;
            screen.changeCategory(idx);
        }
        if (controller.bindings().GUI_PREV_TAB.justPressed()) {
            var idx = screen.getCurrentCategoryIdx() - 1;
            if (idx < 0) idx = screen.config.categories().size() - 1;
            screen.changeCategory(idx);
        }

        super.handleButtons(controller);
    }

    @Override
    public void onWidgetRebuild() {
        ButtonGuideApi.addGuideToButton(screen.finishedSaveButton, bindings -> bindings.GUI_ABSTRACT_ACTION_1, ButtonRenderPosition.TEXT, ButtonGuidePredicate.ALWAYS);
    }

    @Override
    protected void setInitialFocus() {
        screen.setFocused(screen.optionList);
    }
}
