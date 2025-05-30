package dev.isxander.splitscreen.client.features.relaunch;

import dev.isxander.controlify.controller.ControllerUID;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class RelaunchArgument<T> {
    private final String name;
    private @Nullable Optional<T> value;
    private final Function<String, T> toValue;
    private final Function<T, String> toString;

    public static RelaunchArgument<String> string(String name) {
        return new RelaunchArgument<>(name, Function.identity(), Function.identity());
    }

    public static RelaunchArgument<Integer> integer(String name) {
        return new RelaunchArgument<>(name, Integer::parseInt, Object::toString);
    }

    public static RelaunchArgument<Boolean> bool(String name) {
        return new RelaunchArgument<>(name, Boolean::parseBoolean, Object::toString);
    }

    public static RelaunchArgument<ControllerUID> controller(String name) {
        return new RelaunchArgument<>(name, ControllerUID::new, ControllerUID::string);
    }

    public static RelaunchArgument<UUID> uuid(String name) {
        return new RelaunchArgument<>(name, UUID::fromString, UUID::toString);
    }

    private RelaunchArgument(String name, Function<String, T> toValue, Function<T, String> toString) {
        this.name = name;
        this.toValue = toValue;
        this.toString = toString;
    }

    public Optional<T> get() {
        if (value == null) {
            String valueString = System.getProperty(name);
            if (valueString == null) {
                this.value = Optional.empty();
            } else {
                try {
                    this.value = Optional.of(toValue.apply(valueString));
                } catch (Exception e) {
                    e.printStackTrace();
                    this.value = Optional.empty();
                }
            }
        }
        return value;
    }

    public String asArgument(T value) {
        String valueString = toString.apply(value);
        Validate.isTrue(!valueString.contains(" "), "JVM argument cannot contain spaces: `" + valueString + "`");

        return "-D" + name + "=" + valueString;
    }

}
