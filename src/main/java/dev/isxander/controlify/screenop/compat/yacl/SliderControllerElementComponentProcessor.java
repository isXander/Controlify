package dev.isxander.controlify.screenop.compat.yacl;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.component.ComponentProcessor;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.yacl.gui.controllers.slider.SliderControllerElement;

public class SliderControllerElementComponentProcessor implements ComponentProcessor {
    private final SliderControllerElement slider;
    private int ticksSinceIncrement = 0;
    private int lastInput = 0;

    public SliderControllerElementComponentProcessor(SliderControllerElement element) {
        this.slider = element;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller controller) {
        ticksSinceIncrement++;

        var rightStickX = controller.state().axes().rightStickX();
        var rightStickY = controller.state().axes().rightStickY();
        var input = Math.abs(rightStickX) > Math.abs(rightStickY) ? rightStickX : rightStickY;

        if (Math.abs(input) > controller.config().buttonActivationThreshold) {
            if (ticksSinceIncrement > controller.config().screenRepeatNavigationDelay || input != lastInput) {
                slider.incrementValue(lastInput = input > 0 ? 1 : -1);
                ticksSinceIncrement = 0;
                return true;
            }
        } else {
            this.lastInput = 0;
        }

        return false;
    }
}
