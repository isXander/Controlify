package dev.isxander.controlify.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerConfig;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.controller.ControllerType;
import net.minecraft.client.Minecraft;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

public class ClientTestHelper {
    public static void waitForLoadingComplete() {
        waitFor("Loading to complete", client -> client.getOverlay() == null, Duration.ofMinutes(5));
    }

    public static void waitFor(String what, Predicate<Minecraft> condition, Duration timeout) {
        final LocalDateTime end = LocalDateTime.now().plus(timeout);

        while (true) {
            boolean result = submitAndWait(condition::test);

            if (result) break;

            if (LocalDateTime.now().isAfter(end)) {
                throw new RuntimeException("Timed out waiting for " + what + " to complete. (timeout: " + timeout + ")");
            }

            waitFor(Duration.ofSeconds(1));
        }
    }

    public static void waitFor(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> CompletableFuture<T> submit(Function<Minecraft, T> function) {
        return Minecraft.getInstance().submit(() -> function.apply(Minecraft.getInstance()));
    }

    public static <T> T submitAndWait(Function<Minecraft, T> function) {
        return submit(function).join();
    }

    public static Controller<?, ?> createFakeController() {
        return new Controller<>() {
            private final ControllerBindings<ControllerState> bindings = new ControllerBindings<>(this);
            private final ControllerConfig config = new ControllerConfig() {
                @Override
                public void setDeadzone(int axis, float deadzone) {

                }

                @Override
                public float getDeadzone(int axis) {
                    return 0;
                }
            };

            @Override
            public String uid() {
                return "FAKE";
            }

            @Override
            public int joystickId() {
                return -1;
            }

            @Override
            public ControllerBindings<ControllerState> bindings() {
                return bindings;
            }

            @Override
            public ControllerConfig config() {
                return config;
            }

            @Override
            public ControllerConfig defaultConfig() {
                return config;
            }

            @Override
            public void resetConfig() {

            }

            @Override
            public void setConfig(Gson gson, JsonElement json) {

            }

            @Override
            public ControllerType type() {
                return ControllerType.UNKNOWN;
            }

            @Override
            public String name() {
                return "FAKE CONTROLLER";
            }

            @Override
            public ControllerState state() {
                return ControllerState.EMPTY;
            }

            @Override
            public ControllerState prevState() {
                return ControllerState.EMPTY;
            }

            @Override
            public void updateState() {

            }
        };
    }

}
