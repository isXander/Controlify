package dev.isxander.controlify.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.*;
import java.util.function.Supplier;

/**
 * Collection of screens that are in a specific order to set up various aspects of a controller.
 */
public class ControllerSetupWizard {
    private final Deque<Stage> stages = new ArrayDeque<>();
    private final List<Screen> screens = new ArrayList<>();

    public void addStage(Supplier<Boolean> enabled, ScreenCreator screenCreator) {
        this.stages.add(new Stage(enabled, screenCreator));
    }

    public void addStage(ScreenCreator screenCreator) {
        this.addStage(() -> true, screenCreator);
    }

    public Screen start(Screen resultantScreen) {
        Screen prevScreen = resultantScreen;
        while (!stages.isEmpty()) {
            Stage stage = stages.pollLast();
            if (stage.enabled().get()) {
                Screen screen = stage.screenCreator().createWizardScreen(prevScreen);
                if (screen != null) {
                    prevScreen = screen;
                    screens.add(screen);
                }
            }
        }

        return prevScreen;
    }

    public boolean isDone() {
        Screen screen = Minecraft.getInstance().screen;
        return screen == null || screens.stream().noneMatch(resultantScreen -> resultantScreen == screen);
    }

    public interface ScreenCreator {
        Screen createWizardScreen(Screen nextScreen);
    }

    private record Stage(Supplier<Boolean> enabled, ScreenCreator screenCreator) {

    }
}
