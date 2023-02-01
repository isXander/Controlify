package dev.isxander.controlify.compatibility.screen.component;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderComponentProcessor extends ComponentProcessor<AbstractSliderButton> {
    private final Supplier<Boolean> canChangeValueGetter;
    private final Consumer<Boolean> canChangeValueSetter;

    private static final int SLIDER_CHANGE_DELAY = 1;
    private int lastSliderChange = SLIDER_CHANGE_DELAY;

    public SliderComponentProcessor(AbstractSliderButton component, Supplier<Boolean> canChangeValueGetter, Consumer<Boolean> canChangeValueSetter) {
        super(component);
        this.canChangeValueGetter = canChangeValueGetter;
        this.canChangeValueSetter = canChangeValueSetter;
    }

    @Override
    public boolean overrideControllerNavigation(ScreenProcessor screen, Controller controller) {
        if (!this.canChangeValueGetter.get()) return false;

        var canSliderChange = ++lastSliderChange > SLIDER_CHANGE_DELAY;

        var axes = controller.state().axes();
        var buttons = controller.state().buttons();
        if (axes.leftStickX() > 0.5f || buttons.dpadRight()) {
            if (canSliderChange) {
                component.keyPressed(GLFW.GLFW_KEY_RIGHT, 0, 0);
                lastSliderChange = 0;
            }

            return true;
        } else if (axes.leftStickX() < -0.5f || buttons.dpadLeft()) {
            if (canSliderChange) {
                component.keyPressed(GLFW.GLFW_KEY_LEFT, 0, 0);
                lastSliderChange = 0;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor screen, Controller controller) {
        if (!this.canChangeValueGetter.get()) return false;

        if (controller.bindings().GUI_BACK.justPressed()) {
            this.canChangeValueSetter.accept(false);
            return true;
        }

        return false;
    }

    @Override
    public void onNavigateTo(ScreenProcessor screen, Controller controller) {
        this.canChangeValueSetter.accept(false);
    }
}
