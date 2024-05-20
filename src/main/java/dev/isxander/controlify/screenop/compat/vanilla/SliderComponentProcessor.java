package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderComponentProcessor implements ComponentProcessor {
    private final Supplier<Boolean> canChangeValueGetter;
    private final Consumer<Boolean> canChangeValueSetter;

    private final AbstractSliderButton component;

    private static final int SLIDER_CHANGE_DELAY = 1;
    private int lastSliderChange = SLIDER_CHANGE_DELAY;

    public SliderComponentProcessor(AbstractSliderButton component, Supplier<Boolean> canChangeValueGetter, Consumer<Boolean> canChangeValueSetter) {
        this.component = component;
        this.canChangeValueGetter = canChangeValueGetter;
        this.canChangeValueSetter = canChangeValueSetter;
    }

    @Override
    public boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerEntity controller) {
        if (!this.canChangeValueGetter.get()) return false;

        var canSliderChange = ++lastSliderChange > SLIDER_CHANGE_DELAY;

        if (ControlifyBindings.GUI_NAVI_RIGHT.on(controller).digitalNow()) {
            if (canSliderChange) {
                component.keyPressed(GLFW.GLFW_KEY_RIGHT, 0, 0);
                lastSliderChange = 0;
            }

            return true;
        } else if (ControlifyBindings.GUI_NAVI_LEFT.on(controller).digitalNow()) {
            if (canSliderChange) {
                component.keyPressed(GLFW.GLFW_KEY_LEFT, 0, 0);
                lastSliderChange = 0;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
        if (!this.canChangeValueGetter.get()) return false;

        if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
            this.canChangeValueSetter.accept(false);
            return true;
        }

        return false;
    }

    @Override
    public void onFocusGained(ScreenProcessor<?> screen, ControllerEntity controller) {
        this.canChangeValueSetter.accept(false);
    }
}
