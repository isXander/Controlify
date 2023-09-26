package dev.isxander.controlify.screenop;

import net.minecraft.client.gui.screens.Screen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public final class ScreenProcessorFactory {
    private static final Map<Class<? extends Screen>, Factory<?>> factories = new HashMap<>();

    private ScreenProcessorFactory() {
    }

    public static <T extends Screen> ScreenProcessor<? super T> createForScreen(T screen) {
        Class<? extends Screen> screenClass = screen.getClass();
        while (!factories.containsKey(screenClass) && screenClass != Screen.class) {
            screenClass = (Class<? extends Screen>) screenClass.getSuperclass();
        }

        return ((Factory<T>) factories.getOrDefault(screenClass, ScreenProcessorFactory::createDefault)).apply(screen);
    }

    public static <T extends Screen> void registerProvider(Class<T> screenClass, Factory<T> factory) {
        factories.put(screenClass, factory);
    }

    private static <T extends Screen> ScreenProcessor<T> createDefault(T screen) {
        return new ScreenProcessor<>(screen);
    }

    public interface Factory<T extends Screen> extends Function<T, ScreenProcessor<? super T>> {
    }
}
