package dev.isxander.controlify.test;

import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
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

    private static CompletableFuture<Void> submit(Consumer<Minecraft> consumer) {
        return Minecraft.getInstance().submit(() -> consumer.accept(Minecraft.getInstance()));
    }

    public static <T> T submitAndWait(Function<Minecraft, T> function) {
        return submit(function).join();
    }

    public static void submitConsumerAndWait(Consumer<Minecraft> consumer) {
        submit(consumer).join();
    }

    public static void takeScreenshot(String name) {
        AtomicBoolean returned = new AtomicBoolean(false);
        submitAndWait(mc -> {
            Screenshot.grab(mc.gameDirectory, name+".png", mc.getMainRenderTarget(), text -> returned.set(true));
            return true;
        });
        waitFor("Screenshot to be taken", mc -> returned.get(), Duration.ofSeconds(2));

    }

    public static FakeController createAndUseDummyController() {
        var controller = new FakeController();
        Controller.CONTROLLERS.put(controller.uid(), controller);
        controller.use();
        return controller;
    }

}
