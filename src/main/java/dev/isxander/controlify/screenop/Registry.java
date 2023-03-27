package dev.isxander.controlify.screenop;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class Registry<T, U> {
    private final Map<Class<? extends T>, Function<T, U>> registry;
    private final Map<T, U> cache;

    public Registry() {
        this.registry = new HashMap<>();
        this.cache = new HashMap<>();
    }

    /**
     * Registers a constructor for a class
     *
     * @param clazz the class to bind the constructor to
     * @param constructor function to build the object from the class
     * @param <V> type of class
     */
    public <V extends T> void register(Class<V> clazz, Function<V, U> constructor) {
        registry.put(clazz, (Function<T, U>) constructor);
    }

    Optional<U> get(T object) {
        U cached = this.cache.get(object);
        if (cached != null)
            return Optional.of(cached);

        Class<? extends T> clazz = (Class<? extends T>) object.getClass();
        Function<T, U> constructor = registry.get(clazz);
        if (constructor == null)
            return Optional.empty();

        U constructed = constructor.apply(object);
        this.cache.put(object, constructed);
        return Optional.of(constructed);
    }

    @ApiStatus.Internal
    public void clearCache() {
        this.cache.clear();
    }
}
