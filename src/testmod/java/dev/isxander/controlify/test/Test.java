package dev.isxander.controlify.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

public final class Test {
    private final Runnable method;
    private final String name;
    private boolean hasRan;

    public Test(Runnable method, String name) {
        this.method = method;
        this.name = name;
    }

    public void runTest() {
        if (hasRan)
            throw new IllegalStateException("Test `" + name + "` cannot run twice.");

        method.run();
        hasRan = true;
    }

    public String name() {
        return name;
    }

    public boolean hasRan() {
        return hasRan;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Test) obj;
        return Objects.equals(this.method, that.method) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, name);
    }

    @Override
    public String toString() {
        return "Test[" +
                "method=" + method + ", " +
                "name=" + name + ']';
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Entrypoint {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface TitleScreen {
        String value();
    }


}
