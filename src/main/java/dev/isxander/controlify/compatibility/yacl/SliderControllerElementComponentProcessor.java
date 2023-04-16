package dev.isxander.controlify.compatibility.yacl;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.utils.NavigationHelper;
import dev.isxander.yacl.gui.controllers.slider.SliderControllerElement;

public class SliderControllerElementComponentProcessor implements ComponentProcessor {
    private final SliderControllerElement slider;
    private final NavigationHelper navigationHelper = new NavigationHelper(15, 3);

    public SliderControllerElementComponentProcessor(SliderControllerElement element) {
        this.slider = element;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?, ?> controller) {
        var left = controller.bindings().CYCLE_OPT_BACKWARD.held();
        var leftPrev = controller.bindings().CYCLE_OPT_BACKWARD.prevHeld();
        var right = controller.bindings().CYCLE_OPT_FORWARD.held();
        var rightPrev = controller.bindings().CYCLE_OPT_FORWARD.prevHeld();

        boolean repeatEventAvailable = navigationHelper.canNavigate();

        if (left && (repeatEventAvailable || !leftPrev)) {
            slider.incrementValue(-1);

            if (!leftPrev)
                navigationHelper.reset();
        } else if (right && (repeatEventAvailable || !rightPrev)) {
            slider.incrementValue(1);

            if (!rightPrev)
                navigationHelper.reset();
        } else {
            return false;
        }

        navigationHelper.onNavigate();

        return true;
    }
}
