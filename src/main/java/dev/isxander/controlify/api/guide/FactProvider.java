package dev.isxander.controlify.api.guide;

@FunctionalInterface
public interface FactProvider<T> {
    boolean test(T t);

    static <Z> FactProvider<Z> staticProvider(boolean value) {
        return t -> value;
    }
}
